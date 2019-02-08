package dk.makeable.firebasevisionlib

import androidx.appcompat.app.AppCompatActivity

abstract class FirebaseVisionActivity: AppCompatActivity() {

    private var firebaseVision: FirebaseVisionSetup? = null

    /**
     * When this function is called, all setup will be done regarding the camera and permissions, and the camera preview will begin
     * displaying what the camera sees. No further setup needed. The RecognitionProcessor instance will receive updates when it finds
     * any results, and in there is where you should add graphics to the GraphicOverlay.
     *
     * When the user denies the camera permission, this activity will automatically finish.
     */
    fun setupVisionDetection(graphicOverlay: GraphicOverlay,
                             cameraSourcePreview: CameraSourcePreview,
                             recognitionProcessor: RecognitionProcessor,
                             cameraPermissionRationaleString: String,
                             cameraPermissionDeniedString: String) {

        this.attachFirebaseVision(FirebaseVisionSetup(this, graphicOverlay, cameraSourcePreview, recognitionProcessor))
    }

    private fun attachFirebaseVision(visionSetup: FirebaseVisionSetup) {
        this.firebaseVision = visionSetup
    }

}