# My Android Application
Draft version of AR MENU SAD APP

This repository is a prototype for my Senior Design Teams project, Android Version

# Currently runnable ?
Yes.

But not with access to the AR view, only 3D modeler, and only access to 1 model at the moment.


 Also: issues may arise when compiling native-lib.cpp, the Draco library is not included

Why?

In the middle of setting up the Decompression system from Draco (https://github.com/google/draco) 

using Draco involves using C/C++ natively within Android Studio to be called by a JAVA Activity.

There are some struggles working with the NDK, but I'm on the case as we speak ! ;) 

Besides Draco decompression, the rest is up and running

# Latest Update:
Got rid of initial list view of items, and instead have a recycler view being filled with cardViews

uses GeoFire to have right information to access Firebase, and dynamically fill data

Uses Firebase data to gather correct data from AWS S3 storage

note: after adding in a default picture for image in Cards,
everything seems to load at once instead of when finished downloading..
 Will look into later !


Currently working on: downloading models behind the scenes, having some logic to
keep track of all the download processes, Smoothen out activity transitions,
Make a new navigation method that creates circles.

# What API's or librarys are used 
Google Maps

Amazon s3

Firebase

Picasso

OpenGL ES 2.0 - with the help of : https://github.com/andresoviedo/android-3D-model-viewer

Google ARCore                    : https://github.com/google-ar/arcore-android-sdk

Draco                            : https://github.com/google/draco
