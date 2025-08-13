package dk.makeable.firebasevisionlib

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

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

    private var pendingOnPermissionGranted: (() -> Unit)? = null

    private val permissionLauncher: ActivityResultLauncher<String>? = when (owner) {
        is Fragment -> owner.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                pendingOnPermissionGranted?.invoke()
            } else {
                handlePermissionDenied()
            }
            pendingOnPermissionGranted = null
        }

        is ComponentActivity -> owner.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                pendingOnPermissionGranted?.invoke()
            } else {
                handlePermissionDenied()
            }
            pendingOnPermissionGranted = null
        }

        else -> null
    }

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
                // Do NOT override requested camera preview size with view dimensions.
                // Let CameraSource choose based on its high default (1280x960) to maintain quality.
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
        val permissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            onPermissionGranted()
            return
        }

        // Request permission using Activity Result API when possible
        pendingOnPermissionGranted = onPermissionGranted
        if (permissionLauncher != null) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            // Cannot request permission without a Fragment or ComponentActivity owner
            Log.w(
                "FIREBASEVISION",
                "Unable to request CAMERA permission: owner is not Fragment or ComponentActivity"
            )
            handlePermissionDenied()
        }
    }

    private fun handlePermissionDenied() {
        (owner as? Activity)?.finish()
        (owner as? Fragment)?.fragmentManager?.popBackStack()
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