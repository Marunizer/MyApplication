# My Android Application
Draft version of AR MENU SAD APP

This repository is a prototype for my Senior Design Teams project, Android Version

# Currently runnable ?
Yes.

But for testing, only one model can be a seen at the moment. May crash first run for downloading

 Also: issues may arise when compiling native-lib.cpp, the Draco library is not included, as well as ARCore library

Why?

In the middle of setting up the Decompression system from Draco (https://github.com/google/draco) 

using Draco involves using C/C++ natively within Android Studio to be called by a JAVA Activity.

There are some struggles working with the NDK

Besides Draco decompression, the rest is up and running

# Latest Update:
Bug: Models are not dynamically scaled in AR view, Some models come out yellow in normal view


Currently working on: downloading models behind the scenes, having some logic to
keep track of all the download processes, Smoothen out activity transitions,
making models appear correctly

# What API's or librarys are used 
Google Maps

Amazon s3

Firebase

Picasso

OpenGL ES 2.0 - with the help of : https://github.com/andresoviedo/android-3D-model-viewer

Google ARCore                    : https://github.com/google-ar/arcore-android-sdk

Draco                            : https://github.com/google/draco

AND more, will add later