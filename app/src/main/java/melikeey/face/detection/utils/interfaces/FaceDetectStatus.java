package melikeey.face.detection.utils.interfaces;

import melikeey.face.detection.utils.models.RectModel;

public interface FaceDetectStatus {
    void onFaceLocated(RectModel rectModel);
    void onFaceNotLocated() ;
}
