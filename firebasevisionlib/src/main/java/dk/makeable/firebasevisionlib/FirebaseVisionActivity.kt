package dk.makeable.firebasevisionlib

import androidx.appcompat.app.AppCompatActivity

abstract class FirebaseVisionActivity: AppCompatActivity() {

    private lateinit var cameraSource: CameraSource
    private lateinit var cameraSourcePreview: CameraSourcePreview
    private lateinit var graphicOverlay: GraphicOverlay

    /**
     * Return the RecognitionProcessor associated with this activity.
     */
    abstract fun getRecognitionProcessor(): RecognitionProcessor

    /**
     * When this function is called, a call to getRecognitionProcessor will be called immediately after. Will set up the camera.
     * Be sure to follow this up with a call to startCameraSource for the preview to begin updating.
     */
    fun setupVisionDetection(graphicOverlay: GraphicOverlay, cameraSourcePreview: CameraSourcePreview) {
        createCameraSource(graphicOverlay, cameraSourcePreview)
    }

    fun startCameraSource() {
        if (!this::cameraSourcePreview.isInitialized) {
            throw UninitializedPropertyAccessException("CameraSource is not initialized, be sure to call setupVisionDetection before starting/stopping the CameraSource.")
        }
        cameraSourcePreview.start(cameraSource, graphicOverlay)
    }

    fun stopCameraSource() {
        if (!this::cameraSourcePreview.isInitialized) {
            throw UninitializedPropertyAccessException("CameraSource is not initialized, be sure to call setupVisionDetection before starting/stopping the CameraSource.")
        }
        cameraSourcePreview.stop()
    }

    fun releaseCameraSource() {
        if (!this::cameraSourcePreview.isInitialized) {
            throw UninitializedPropertyAccessException("CameraSource is not initialized, be sure to call setupVisionDetection before starting/stopping the CameraSource.")
        }
        cameraSource.release()
    }

    private fun createCameraSource(graphicOverlay: GraphicOverlay, cameraSourcePreview: CameraSourcePreview) {
        this.graphicOverlay = graphicOverlay
        this.cameraSourcePreview = cameraSourcePreview
        cameraSource = CameraSource(this, graphicOverlay)

        // Set the processor
        cameraSource.setMachineLearningFrameProcessor(getRecognitionProcessor())
    }

}