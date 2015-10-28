//
//  FIFTagHandlerPlugin
//  Fifty-five
//
//  Created by Med on 09/12/14.
//  Copyright (c) 2014 Med. All rights reserved.

package com.fiftyfive.fiftaghandler;


import com.fiftyfive.fiftaghandler.FIFTagHandler;

import android.content.Context;

import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;

import com.fiftyfive.tagHandler.FB.v4_5_1.FBFunctionCallTagHandler;
import com.fiftyfive.tagHandler.GA.v4_0_0.GAFunctionCallMacroHandler;
import com.fiftyfive.tagHandler.MAT.v3_4_2.MATFunctionCallTagHandler;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tagmanager.ContainerHolder;
import com.google.android.gms.tagmanager.DataLayer;
import com.google.android.gms.tagmanager.TagManager;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Iterator;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class FIFTagHandlerPlugin extends CordovaPlugin {
    
    private static TagManager tagManager;
    
    /**
     * Initializes the plugin
     *
     * @param cordova The context of the main Activity.
     * @param webView The associated CordovaWebView.
     */
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }
    
    /**
     * Executes the request.
     *
     * @param action          The action to execute.
     * @param args            The exec() arguments.
     * @param Callback        The callback context used when calling back into JavaScript.
     * @return                Whether the action was valid.
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callback) throws JSONException {
        try {
            if ("setContainerId".equals(action)) {
                setContainerId(args.getString(0));
                callback.success();
                return true;
            } else if ("push".equals(action)) {
                push(objectMap(args.getJSONObject(0)));
                callback.success();
                return true;
            } 
            else if ("setVerboseLoggingEnabled".equals(action)) {
                setVerboseLoggingEnabled();
            } 
        } catch (Exception e) {
            callback.error(e.getMessage());
        }
        return false;
    }
    
    private void setContainerId(String containerId) {
        //Launch Tag Manager
        Context context = this.cordova.getActivity().getApplicationContext();
        tagManager = TagManager.getInstance(context);


        //Launch FFTagHelper
        FIFTagHandler.getInstance().setTagManager(tagManager);
        FIFTagHandler.getInstance().setApplicationContext(context);


        int gtmResourceId = this.cordova.getActivity().getResources().getIdentifier(
                "raw/gtm_default_container",
                "id",
                this.cordova.getActivity().getPackageName()
        );

        //GTM
        PendingResult<ContainerHolder> pending =
                tagManager.loadContainerPreferNonDefault(containerId,
                        gtmResourceId);

        pending.setResultCallback(new ResultCallback<ContainerHolder>() {
            @Override
            public void onResult(ContainerHolder containerHolder) {
                FIFTagHandler.getInstance().setContainerHolder(containerHolder);
                containerHolder.refresh();

                if (!containerHolder.getStatus().isSuccess()) {
                    return;
                }

                //manuel registering
                MATFunctionCallTagHandler mat = new MATFunctionCallTagHandler();
                mat.register(containerHolder.getContainer());
                GAFunctionCallMacroHandler ga = new GAFunctionCallMacroHandler();
                ga.register(containerHolder.getContainer());
                FBFunctionCallTagHandler fb = new FBFunctionCallTagHandler();
                fb.register(containerHolder.getContainer());

                DataLayer dataLayer = FIFTagHandler.getInstance().getTagManager().getDataLayer();
                GoogleAnalytics analytics = GoogleAnalytics.getInstance(FIFTagHandler.getInstance().getApplicationContext());
                Tracker tracker = analytics.newTracker("UA-1111-1");
                String cid = tracker.get("&cid");

                // Push application start event
                Map<String, Object> appStartMap = new HashMap<String, Object>();
                appStartMap.put("event", "applicationStart");
                appStartMap.put("userGoogleId",cid)
                dataLayer.push(appStartMap);
            }
        }, 2, TimeUnit.SECONDS);
        

    }

    private void setVerboseLoggingEnabled(){
         if (tagManager == null) {
            throw new IllegalStateException("FIFTagHandler not initialized. Call setContainerId first.");
        }
        else{
             tagManager.setVerboseLoggingEnabled(true);
        }
       
    }

    private void push(Map<String, Object> map) {
        // Fetch the datalayer
        if (tagManager == null) {
            throw new IllegalStateException("FIFTagHelper not initialized. Call setContainerId.");
        }
        else {
            DataLayer dataLayer = tagManager.getDataLayer();
            dataLayer.push(map);
        }
    }
    


    private Map<String, Object> objectMap(JSONObject o) throws JSONException {
        if (o.length() == 0) {
            return Collections.<String, Object>emptyMap();
        }
        Map<String, Object> map = new HashMap<String, Object>(o.length());
        Iterator it = o.keys();
        String key;
        Object value;
        while (it.hasNext()) {
            key = it.next().toString();
            value = o.has(key) ? o.get(key): null;
            map.put(key, value);
        }
        return map;
    }
}
