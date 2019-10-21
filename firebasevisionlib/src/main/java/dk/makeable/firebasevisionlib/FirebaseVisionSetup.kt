package dk.makeable.firebasevisionlib

import android.Manifest
import android.app.Activity
import android.content.Context
import android.hardware.Camera
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.internal.phenotype.zzh.init
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission

/**
 * This class uses a reference to an context, and a reference to a CameraSourcePreview/GraphicOverlay to control and release the camera when needed based on the activities lifecycle.
 */
class FirebaseVisionSetup<T> (
    private val owner: T,
    private val context: Context,
    private val graphicOverlay: GraphicOverlay,
    private val cameraSourcePreview: CameraSourcePreview,
    private val recognitionProcessor: RecognitionProcessor,
    private val rationaleString: String,
    private val deniedString: String
): LifecycleObserver where T: LifecycleOwner {

    private val cameraSource: CameraSource = CameraSource(context, graphicOverlay)

    private var started: Boolean = false
    private var isStarting: Boolean = false

    init {
        cameraSource.setMachineLearningFrameProcessor(recognitionProcessor)

        // Register this object to the context lifecycle
        owner.lifecycle.addObserver(this)

        // Register this object with the VisionSetupManager
        VisionSetupManager.instance.registerVisionSetup(this)

        start() // Start if not already.
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun start() {
        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED) && !started && !isStarting) {
            isStarting = true
            secureCameraPermission {
                started = true
                Log.d("FIREBASEVISION", "Starting cameraSource with preview width: ${cameraSourcePreview.width}, height: ${cameraSourcePreview.height}")
                cameraSource.setRequestedCameraPreviewSize(cameraSourcePreview.width, cameraSourcePreview.height)
                cameraSourcePreview.start(cameraSource, graphicOverlay)
                isStarting = false
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

        // Unregister the setup
        VisionSetupManager.instance.unregisterVisionSetup(this)
    }

    private fun secureCameraPermission(onPermissionGranted: () -> Unit) {
        // Ask the user for permission to use the camera
        TedPermission.with(context)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    onPermissionGranted()
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    (owner as? Activity)?.finish()
                    (owner as? Fragment)?.fragmentManager?.popBackStack()
//                    owner.finish() // Finish the visionActivity
                }
            })
            .setRationaleMessage(rationaleString)
            .setDeniedMessage(deniedString)
            .setPermissions(Manifest.permission.CAMERA)
            .check()
    }

    /**
     * Will reload the camera with the given recognition processor, now recognizing the things for that processor.
     */
    public fun setRecognitionProcessor(processor: RecognitionProcessor) {
        cameraSource.setMachineLearningFrameProcessor(processor)
    }

    /**
     * Toggles the flashlight if available
     */
    public fun toggleFlashlight(enabled: Boolean) {
        cameraSource.toggleFlashlight(enabled)
    }

    /**
     * Sets the focusMode on the Camera, IF AND ONLY IF it is supported by the camera.
     */
    public fun setFocusMode(focusMode: String) {
        cameraSource.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO)
    }

}