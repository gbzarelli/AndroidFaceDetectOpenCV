Native OpenCV in Android Studio with face detect
=================================

This application is a sample Android Studio project (tested on gradle 3.0.1) with 'Android SDK OpenCV 3.3.1' 
(https://sourceforge.net/projects/opencvlibrary/files/opencv-android/)

----

Usage:

Here is how to use this project to run native OpenCV code.

* Make sure you have Android SDK up to date, with NDK installed
* Download latest OpenCV SDK for Android from OpenCV.org and decompress the zip file.
* Clone this project
* Create a symlink named `jniLibs` in `app/src/main` that points to `YOUR_OPENCV_SDK/sdk/native/libs`
* In `app/CMakeLists.txt` change line 9 to points to `YOUR_OPENCV_SDK/sdk/native/jni/include`
* Sync gradle
* Run the application

----

Utils:

The openCVLibaray331 library inside the project contains the java code '/openCVLibrary331/src/main/java' copied from the `YOUR_OPENCV_SDK`/sdk/java/src folder.
The aidl folder '/openCVLibrary331/src/main/aidl' contains the contents of `YOUR_OPENCV_SDK`/sdk/java/src/org/opencv/engine folder, keeping the same package.

For detection we are using a Haar Cascade frontal face detection inserted in res/raw/haarcascade_frontalface_alt.xml

We can find other Haar at:
https://github.com/opencv/opencv/tree/master/data/haarcascades

----

References: https://github.com/leadrien/opencv_native_androidstudio