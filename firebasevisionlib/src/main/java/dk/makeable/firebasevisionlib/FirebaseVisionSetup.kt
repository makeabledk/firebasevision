package dk.makeable.firebasevisionlib

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

/**
 * This class uses a reference to an activity, and a reference to a CameraSourcePreview/GraphicOverlay to control and release the camera when needed based on the activities lifecycle.
 */
class FirebaseVisionSetup(private val activity: AppCompatActivity,
                          private val graphicOverlay: GraphicOverlay,
                          private val cameraSourcePreview: CameraSourcePreview,
                          private val recognitionProcessor: RecognitionProcessor): LifecycleObserver {

    private val cameraSource: CameraSource = CameraSource(activity, graphicOverlay)

    private var started: Boolean = false

    init {
        cameraSource.setMachineLearningFrameProcessor(recognitionProcessor)

        // Register this object to the activity lifecycle
        activity.lifecycle.addObserver(this)

        start() // Start if not already.
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun start() {
        if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED) && !started) {
            started = true
            cameraSourcePreview.start(cameraSource, graphicOverlay)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun stop() {
        if (started) {
            started = false
            cameraSourcePreview.stop()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun release() {
        cameraSource.release()
    }

}