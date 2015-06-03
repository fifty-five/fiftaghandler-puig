FIFTagHandler cordova plugin
============================

* [Introduction](#introduction)
* [Installation](#installation)
* [Getting started](#getting-started)

## Introduction

This plugin provides Apache Cordova/Phonegap support for the FIFTagHandler using the native sdks for Android & iOS. The FIFTagHandler SDK is a wrapper above the Google Tag Manager SDK. It enable iOS/Android developers to track their apps using only GTM. The FIFTagHandler SDK currently support the following trackers: Google Analytics, Facebook, Localytics, ATInternet, MobileAppTracker and Follow Analtics.

Android Native SDK v4 (using Google Play Services SDK)
iOS Native SDK v3
This plugin provides support for some of the most specific analytics functions (screen, event & exception tracking, custom metrics & dimensions) and also the more generic set and send functions which can be used to implement all of the Google Analytics collection features.

## Installation

To install the FIFTagHandler plugin in your app, use the following command-line


```shell

	cordova plugin add https://github.com/fifty-five/fiftaghandler-puig

```


#### Configure the Facebook SDK

Once the FIFTagHandler plugin for cordova is installed, make sure to edit both iOS and Android projects to add the Facebook app id and the facebook app name. For more details, please follow instructions of the [Facebook documentation](https://developers.facebook.com/docs/)


## Getting started

#### Initialize the FIFTagHandler SDK

To initialize the FIFTagHandler SDK, use the `setContainerId` function. Make sure to pass the Google Tag Manager container identifier as parameter.

```js

	var fiftaghandler = navigator.fiftaghandler;
	
	// Init using the GTM container id
	fiftaghandler.setContainerId('GTM-XXXX');

```

#### Push key / value object to the DataLayer

Once the FIFTagHandler SDK is initialized, you can push events / data to the Google Tag Manager DataLayer. Using the `push` function to push any key/value pair. For instance, just after initializing the FIFTagHandler, we push an appStarted event to the DataLayer

```js

	// Push appStarted event to the DataLayer
	fiftaghandler.push({'event': 'applicationStart'});

```

#### Must read : order of push in fiftaghandler is important 

When you push data to the dataLayer, it is very important that all values are set before the events. 

For instance on a screen, when we asked for 
- event:openScreen 
- screenName : splashScreen 
- userStatus : connected 

You could implemented it like that : 

######Good implementation sequence :

In Google Tag Manager, the keys screenName and userStatus will be defined 
```
fiftaghandler.push(
	{'event': 'applicationStart',
	 'screenName': 'splashScreen',
	 'userStatus' : 'connected'});
```
In Google Tag Manager, the keys screenName and userStatus will be defined 
```
fiftaghandler.push({'screenName': 'splashScreen'});
fiftaghandler.push({'userStatus': 'connected'});
fiftaghandler.push({'event': 'applicationStart'});
```


######Bad implementation sequence : 
With this implementation, the keys screenName and userStatus won't be defined for the event 'applicationStart'
```
fiftaghandler.push({'event': 'applicationStart'});
fiftaghandler.push({'screenName': 'splashScreen'});
fiftaghandler.push({'userStatus': 'connected'});
```

