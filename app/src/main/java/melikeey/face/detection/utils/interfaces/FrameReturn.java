package melikeey.face.detection.utils.interfaces;

import android.graphics.Bitmap;

import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import melikeey.face.detection.utils.common.FrameMetadata;
import melikeey.face.detection.utils.common.GraphicOverlay;

public interface FrameReturn{
    void onFrame(
            Bitmap image ,
            FirebaseVisionFace face ,
            FrameMetadata frameMetadata,
            GraphicOverlay graphicOverlay
    );
}