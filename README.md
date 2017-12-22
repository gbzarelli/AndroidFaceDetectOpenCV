Native OpenCV in Android Studio with face detect
=================================

This application is a sample Android Studio project (tested on gradle 3.0.1) with 'Android SDK OpenCV 3.3.1' 
(https://sourceforge.net/projects/opencvlibrary/files/opencv-android/)

Usage
-----

Here is how to use this project to run native OpenCV code.

* Make sure you have Android SDK up to date, with NDK installed
* Download latest OpenCV SDK for Android from OpenCV.org and decompress the zip file.
* Clone this project
* Create a symlink named `jniLibs` in `app/src/main` that points to `YOUR_OPENCV_SDK/sdk/native/libs`
* In `app/CMakeLists.txt` change line 9 to points to `YOUR_OPENCV_SDK/sdk/native/jni/include`
* Sync gradle
* Run the application

References: https://github.com/leadrien/opencv_native_androidstudio
