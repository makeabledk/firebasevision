package dk.makeable.firebasevisionlib

import androidx.appcompat.app.AppCompatActivity

abstract class FirebaseVisionActivity: AppCompatActivity() {

    private var firebaseVision: FirebaseVisionSetup? = null

    /**
     * When this function is called, a call to getRecognitionProcessor will be called immediately after. Will set up the camera.
     * Be sure to follow this up with a call to startCameraSource for the preview to begin updating.
     */
    fun setupVisionDetection(graphicOverlay: GraphicOverlay,
                             cameraSourcePreview: CameraSourcePreview,
                             recognitionProcessor: RecognitionProcessor) {

        this.attachFirebaseVision(FirebaseVisionSetup(this, graphicOverlay, cameraSourcePreview, recognitionProcessor))
//        createCameraSource(graphicOverlay, cameraSourcePreview)
    }

    private fun attachFirebaseVision(visionSetup: FirebaseVisionSetup) {
        this.firebaseVision = visionSetup
    }

//    fun startCameraSource() {
//        if (!this::cameraSourcePreview.isInitialized) {
//            throw UninitializedPropertyAccessException("CameraSource is not initialized, be sure to call setupVisionDetection before starting/stopping the CameraSource.")
//        }
//        cameraSourcePreview.start(cameraSource, graphicOverlay)
//    }
//
//    fun stopCameraSource() {
//        if (!this::cameraSourcePreview.isInitialized) {
//            throw UninitializedPropertyAccessException("CameraSource is not initialized, be sure to call setupVisionDetection before starting/stopping the CameraSource.")
//        }
//        cameraSourcePreview.stop()
//    }
//
//    fun releaseCameraSource() {
//        if (!this::cameraSourcePreview.isInitialized) {
//            throw UninitializedPropertyAccessException("CameraSource is not initialized, be sure to call setupVisionDetection before starting/stopping the CameraSource.")
//        }
//        cameraSource.release()
//    }
//
//    private fun createCameraSource(graphicOverlay: GraphicOverlay, cameraSourcePreview: CameraSourcePreview) {
//        this.graphicOverlay = graphicOverlay
//        this.cameraSourcePreview = cameraSourcePreview
//        cameraSource = CameraSource(this, graphicOverlay)
//
//        // Set the processor
//        cameraSource.setMachineLearningFrameProcessor(getRecognitionProcessor())
//    }

}