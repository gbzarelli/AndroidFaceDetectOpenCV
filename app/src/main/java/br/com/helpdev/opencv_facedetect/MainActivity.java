package br.com.helpdev.opencv_facedetect;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * https://github.com/leadrien/opencv_native_androidstudio
 * Usage
 * <p>
 * Here is how to use this project to run native OpenCV code.
 * <p>
 * - Make sure you have Android SDK up to date, with NDK installed
 * - Download latest OpenCV SDK for Android from OpenCV.org and decompress the zip file.
 * - Create a symlink named jniLibs in app/src/main that points to YOUR_OPENCV_SDK/sdk/native/libs
 * - In app/CMakeLists.txt change line 9 to points to YOUR_OPENCV_SDK/sdk/native/jni/include
 * - Sync gradle
 * - Run the application
 */
public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, Runnable {

    private static final String TAG = "OCVSample::Activity";
    private CameraBridgeViewBase _cameraBridgeViewBase;

    private BaseLoaderCallback _baseLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    // Load ndk built module, as specified in moduleName in build.gradle
                    // after opencv initialization
                    System.loadLibrary("native-lib");
                    _cameraBridgeViewBase.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
            }
        }
    };
    private volatile boolean proc = false;
    private volatile boolean running = false;
    private volatile Mat matTmp;

    private CascadeClassifier cascadeClassifier;
    private File mCascadeFile;
    private TextView info;
    private MatOfRect matOfRect;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        info = findViewById(R.id.tv);
        try {
            loadFileCascade();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        _cameraBridgeViewBase = findViewById(R.id.main_surface);
        _cameraBridgeViewBase.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        _cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        _cameraBridgeViewBase.setCvCameraViewListener(this);
    }

    private void loadFileCascade() throws Throwable {
        File cascadeDir = getDir("haarcascade_frontalface_alt", Context.MODE_PRIVATE);
        mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt.xml");

        if (!mCascadeFile.exists()) {
            FileOutputStream os = new FileOutputStream(mCascadeFile);
            InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        disableCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            _baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, _baseLoaderCallback);
        }
        cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        cascadeClassifier.load(mCascadeFile.getAbsolutePath());
        startFaceDetect();
    }

    public void onDestroy() {
        super.onDestroy();
        disableCamera();
    }

    public void disableCamera() {
        running = false;
        if (_cameraBridgeViewBase != null)
            _cameraBridgeViewBase.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }


    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if (!proc) {
            matTmp = inputFrame.rgba();
        }
        return inputFrame.rgba();
    }


    public void startFaceDetect() {
        if (running) return;
        new Thread(this).start();
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            
            try {
                if (matTmp != null) {
                    proc = true;
                    matOfRect = new MatOfRect();
                    cascadeClassifier.detectMultiScale(matTmp, matOfRect);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            info.setText(String.format(getString(R.string.faces_detects), matOfRect.toList().size()));
                        }
                    });
                }
                Thread.sleep(200);
            } catch (Throwable t) {
                Log.e(TAG, "proc", t);
            } finally {
                proc = false;
            }
        }
    }
}

