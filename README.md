# My Android Application
Draft version of AR MENU SAD APP

This repository is a prototype for my Senior Design Teams project, Android Version

projects 3D models in an intractable viewer + AR in a user friendly experience
# Currently runnable ?
Yes.

But for testing, only one model can be accessed to the public. First attempt may not work because I have taken out the handler that manages what to do when a download is complete. Restarting app will make it functional.

 Also: issues may arise when compiling native-lib.cpp, the Draco library is not included, as well as ARCore library

Why?

In the middle of setting up the Decompression system from Draco (https://github.com/google/draco) 

using Draco involves using C/C++ natively within Android Studio to be called by a JAVA Activity.

There are some struggles working with the NDK

Besides Draco decompression, the rest is up and running

Possible fix:

Remove everything C++ related from the gradle file and don't include Cmake before building project.

# Latest Issue:
Bug: Models are not dynamically scaled in AR view, AR not capable of rendering files with no smooth groups or normal vectors


Currently working on: downloading models behind the scenes, having some logic to
keep track of all the download processes. Draco decompression. Finding alternate obj builder libraries.

# What API's or librarys are used 
Google Maps

Firebase realtime database / storage  / geoFire

Glide

OpenGL ES 2.0 - with the help of : https://github.com/andresoviedo/android-3D-model-viewer

JavaGL                           : https://github.com/javagl/Obj

Google ARCore                    : https://github.com/google-ar/arcore-android-sdk

Draco                            : https://github.com/google/draco

Material Library(s):
https://github.com/pungrue26/SelectableRoundedImageView

AND more, will add later...