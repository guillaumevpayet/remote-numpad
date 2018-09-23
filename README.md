# Remote Numpad (Android project)

## Introduction

This project is a part of a trio of projects:

* Remote Numpad (this project): Written in Kotlin, this is the client which
runs on an Android device and sends the user's inputs to the computer.
* [Remote Numpad Server](https://github.com/theolizard/remote-numpad-server):
Written in Kotlin, this is the server that runs on the computer and receives
the inputs from the Android device and simulates the key presses.
* [Cocoa Native Server](https://github.com/theolizard/cocoa-native-server):
Written in Objective-C, this is the Bluetooth server library for MacOS X. It
receives the Bluetooth data and passes it on to the Remote Numpad Server.

## Description

This is an Android project written in Kotlin and compatible with Android
devices down to API level 16. It is made up of 2 activities, the main
*NumpadActivity* and the *SettingsActivity*.

The *NumpadActivity* is the activity that displays the numpad using a
*ConstraintLayout* and sending them through an *IConnectionInterface* that is
decided by the user preferences. So far, wifi and classic Bluetooth (RFCOMM)
are supported but BLE and Wifi-Direct are intended to be implemented in the
future.

The *SettingsActivity* is the activity which allows the user to select the
interface through which to send the inputs and the location of the server.

The lastest stable version can be obtained from the
[Google Play Store](https://play.google.com/store/apps/details?id=com.guillaumepayet.remotenumpad&hl=en_US)

## Compilation

The project is set up with Android Studio so it can be compiled using Gradle
or imported by any Gradle-capable and Android-capable IDE (e.g.: Android
Studio, IntelliJ, Eclipse) and then compiled.

## Contributing

This is not a main project for me so help is very apreciated. Anyone is
welcome to contribute to this project (issues, requests, pull requests).
