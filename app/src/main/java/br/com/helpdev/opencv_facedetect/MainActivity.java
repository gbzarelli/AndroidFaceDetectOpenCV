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

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, Runnable {

    private static final String TAG = "OCVSampleFaceDetect";
    private CameraBridgeViewBase cameraBridgeViewBase;

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");
                cameraBridgeViewBase.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    private volatile boolean running = false;
    private volatile int qtdFaces;
    private volatile Mat matTmpProcessingFace;

    private CascadeClassifier cascadeClassifier;
    private File mCascadeFile;
    private TextView infoFaces;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        infoFaces = findViewById(R.id.tv);
        try {
            loadFileCascade();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        cameraBridgeViewBase = findViewById(R.id.main_surface);
        cameraBridgeViewBase.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
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
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        }
        cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        cascadeClassifier.load(mCascadeFile.getAbsolutePath());
        startFaceDetect();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if (matTmpProcessingFace == null) {
            matTmpProcessingFace = inputFrame.gray();
        }
        return inputFrame.rgba();
    }


    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
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
                if (matTmpProcessingFace != null) {
                    MatOfRect matOfRect = new MatOfRect();
                    cascadeClassifier.detectMultiScale(matTmpProcessingFace, matOfRect);
                    int newQtdFaces = matOfRect.toList().size();
                    if (qtdFaces != newQtdFaces) {
                        qtdFaces = newQtdFaces;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                infoFaces.setText(String.format(getString(R.string.faces_detects), qtdFaces));
                            }
                        });
                    }
                    Thread.sleep(500);//if you want an interval
                    matTmpProcessingFace = null;
                }
                Thread.sleep(50);
            } catch (Throwable t) {
                try {
                    Thread.sleep(10_000);
                } catch (Throwable tt) {
                }
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        disableCamera();
    }

    public void disableCamera() {
        running = false;
        if (cameraBridgeViewBase != null)
            cameraBridgeViewBase.disableView();
    }

}

