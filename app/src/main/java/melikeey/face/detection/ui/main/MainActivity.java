// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package melikeey.face.detection.ui.main;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.annotation.KeepName;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.hsalf.smilerating.BaseRating;
import com.hsalf.smilerating.SmileRating;

import java.io.IOException;

import melikeey.face.detection.R;
import melikeey.face.detection.utils.base.BaseActivity;
import melikeey.face.detection.utils.common.CameraSource;
import melikeey.face.detection.utils.common.CameraSourcePreview;
import melikeey.face.detection.utils.interfaces.FaceDetectStatus;
import melikeey.face.detection.utils.common.FrameMetadata;
import melikeey.face.detection.utils.interfaces.FrameReturn;
import melikeey.face.detection.utils.common.GraphicOverlay;
import melikeey.face.detection.utils.base.PublicMethods;
import melikeey.face.detection.utils.models.RectModel;
import melikeey.face.detection.utils.visions.FaceDetectionProcessor;



@KeepName
public final class MainActivity extends BaseActivity implements OnRequestPermissionsResultCallback, FrameReturn, FaceDetectStatus {

    private static final String FACE_DETECTION = "Face Detection";
    private static final String TAG = "MLKitTAG";

    Bitmap originalImage = null;
    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private ImageView faceFrame;
    private SmileRating smile_rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preview = findViewById(R.id.firePreview);
        faceFrame = findViewById(R.id.faceFrame);
        graphicOverlay = findViewById(R.id.fireFaceOverlay);
        smile_rating = findViewById(R.id.smile_rating);

        if (PublicMethods.allPermissionsGranted(this)) {
            createCameraSource();
        } else {
            PublicMethods.getRuntimePermissions(this);
        }

    }


    private void createCameraSource() {
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }
        try {
            FaceDetectionProcessor processor = new FaceDetectionProcessor(getResources());
            processor.frameHandler = this;
            processor.faceDetectStatus = this;
            cameraSource.setMachineLearningFrameProcessor(processor);
        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor: " + FACE_DETECTION, e);
            Toast.makeText(
                    getApplicationContext(),
                    "Can not create image processor: " + e.getMessage(),
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PublicMethods.allPermissionsGranted(this)) {
            createCameraSource();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //calls with each frame includes by face
    @Override
    public void onFrame(Bitmap image, FirebaseVisionFace face, FrameMetadata frameMetadata, GraphicOverlay graphicOverlay) {
        originalImage = image;
        if (face.getLeftEyeOpenProbability() < 0.4) {
            findViewById(R.id.rightEyeStatus).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.rightEyeStatus).setVisibility(View.INVISIBLE);
        }
        if (face.getRightEyeOpenProbability() < 0.4) {
            findViewById(R.id.leftEyeStatus).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.leftEyeStatus).setVisibility(View.INVISIBLE);
        }

        int smile = 0;

        if (face.getSmilingProbability() > .8) {
            smile = BaseRating.GREAT ;
        } else if (face.getSmilingProbability() <= .8 && face.getSmilingProbability() > .6) {
            smile = BaseRating.GOOD ;
        } else if (face.getSmilingProbability() <= .6 && face.getSmilingProbability() > .4) {
            smile = BaseRating.OKAY ;
        } else if (face.getSmilingProbability() <= .4 && face.getSmilingProbability() > .2) {
            smile = BaseRating.BAD ;
        }
        smile_rating.setSelectedSmile(smile, true);

    }

    @Override
    public void onFaceLocated(RectModel rectModel) {
        faceFrame.setColorFilter(ContextCompat.getColor(this, R.color.green));

    }

    @Override
    public void onFaceNotLocated() {
        faceFrame.setColorFilter(ContextCompat.getColor(this, R.color.red));
    }
}
