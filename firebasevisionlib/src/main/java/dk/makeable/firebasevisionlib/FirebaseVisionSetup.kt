package dk.makeable.firebasevisionlib

import android.Manifest
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission

/**
 * This class uses a reference to an activity, and a reference to a CameraSourcePreview/GraphicOverlay to control and release the camera when needed based on the activities lifecycle.
 */
class FirebaseVisionSetup(
    private val activity: FirebaseVisionActivity,
    private val graphicOverlay: GraphicOverlay,
    private val cameraSourcePreview: CameraSourcePreview,
    private val recognitionProcessor: RecognitionProcessor,
    private val rationaleString: String,
    private val deniedString: String
) : LifecycleObserver {

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
            secureCameraPermission {
                started = true
                Log.d("FIREBASEVISION", "Starting cameraSource with preview width: ${cameraSourcePreview.width}, height: ${cameraSourcePreview.height}")
                cameraSource.setRequestedCameraPreviewSize(cameraSourcePreview.width, cameraSourcePreview.height)
                cameraSourcePreview.start(cameraSource, graphicOverlay)
            }
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

    private fun secureCameraPermission(onPermissionGranted: () -> Unit) {
        // Ask the user for permission to use the camera
        TedPermission.with(activity)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    onPermissionGranted()
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    activity.finish() // Finish the visionActivity
                }
            })
            .setRationaleMessage(rationaleString)
            .setDeniedMessage(deniedString)
            .setPermissions(Manifest.permission.CAMERA)
            .check()
    }

}