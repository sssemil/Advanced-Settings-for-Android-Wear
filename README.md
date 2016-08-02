# Advanced Settings for Android Wear [![Build Status](https://travis-ci.org/sssemil/Advanced-Settings-for-Android-Wear.png)](https://travis-ci.org/sssemil/Advanced-Settings-for-Android-Wear)
Advanced Settings for android wear devices. Root access is required for some features to work. 

Here is transifex page for translation - https://www.transifex.com/sssemil/advanced-settings/

How to build
------------

1. Install the [Android Studio](https://developer.android.com/studio/index.html) 
2. Clone this repo
3. Open it within Android Studio

Adding new device
-----------------

In wear/src/main/assets you can see some config files, just copy any of them and name it after your device name (you can find it in /system/build.prop). After that explore your watch and change values to fit your it.

### What you get (marked with R require root access):
+ Vibration intensity changing (R)
+ Installed apps information
+ Running apps information
+ Removing apps from watch(without phone or PC) (R)
+ Installing apps on watch(without phone or PC, but you need to install some file manager) (R)
+ Disabling system apps (R)
+ Change screen timeout(affects only in-apps timeout, does not affect main screen timeout)
+ Wider brightness settings
+ Alert on disconnect from phone
+ Bluetooth settings
+ Wi-Fi settings(if supported)
+ Wake on touch changing (R)
+ Date & time settings (R)
+ System language changing
