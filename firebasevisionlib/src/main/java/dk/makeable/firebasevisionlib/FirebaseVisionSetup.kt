package dk.makeable.firebasevisionlib

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

/**
 * This class uses a reference to an context, and a reference to a CameraSourcePreview/GraphicOverlay to control and release the camera when needed based on the activities lifecycle.
 * Note: The Activity or Fragment using this class must call handlePermissionResult() in their onRequestPermissionsResult() method.
 */
class FirebaseVisionSetup<T> (
    private val owner: T,
    private val context: Context,
    private val graphicOverlay: GraphicOverlay,
    private val cameraSourcePreview: CameraSourcePreview,
    private val recognitionProcessor: RecognitionProcessor,
    private val rationaleString: String,
    private val deniedString: String
) : DefaultLifecycleObserver where T : LifecycleOwner {

    private val cameraSource: CameraSource = CameraSource(context, graphicOverlay)

    private var started: Boolean = false
    private var isStarting: Boolean = false
    private var pendingPermissionCallback: (() -> Unit)? = null
    private val permissionLauncher: ActivityResultLauncher<String>?

    companion object {
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
    }

    init {
        permissionLauncher = (owner as? ActivityResultCaller)?.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                pendingPermissionCallback?.invoke()
            } else {
                // Permission denied – mimic old behaviour
                (owner as? Activity)?.finish()
                (owner as? Fragment)?.parentFragmentManager?.popBackStack()
            }
            pendingPermissionCallback = null
        }

        cameraSource.setMachineLearningFrameProcessor(recognitionProcessor)

        // Register this object to the context lifecycle
        owner.lifecycle.addObserver(this)

        // Register this object with the VisionSetupManager
        VisionSetupManager.instance.registerVisionSetup(this)

        start() // Start if not already.
    }

    override fun onResume(owner: LifecycleOwner) {
        start()
    }

    private fun start() {
        if (!started && !isStarting) {
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

    override fun onPause(owner: LifecycleOwner) {
        stop()
    }

    private fun stop() {
        if (started) {
            started = false
            cameraSourcePreview.stop()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        release()
    }

    private fun release() {
        cameraSource.release()

        // Unregister the setup
        VisionSetupManager.instance.unregisterVisionSetup(this)
    }

    private fun secureCameraPermission(onPermissionGranted: () -> Unit) {
        // Check if camera permission is already granted
        if (ContextCompat.checkSelfPermission(
                context,
                CAMERA_PERMISSION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onPermissionGranted()
            return
        }

        pendingPermissionCallback = onPermissionGranted
        // Launch permission request via Activity Result API
        if (permissionLauncher != null) {
            permissionLauncher.launch(CAMERA_PERMISSION)
        } else {
            Log.e(
                "FIREBASEVISION",
                "Permission launcher is null. Owner must implement ActivityResultCaller."
            )
            // Fallback to legacy behaviour
            (owner as? Activity)?.let { act ->
                ActivityCompat.requestPermissions(act, arrayOf(CAMERA_PERMISSION), 0)
            }
        }
    }

    /**
     * Will reload the camera with the given recognition processor, now recognizing the things for that processor.
     */
    fun setRecognitionProcessor(processor: RecognitionProcessor) {
        cameraSource.setMachineLearningFrameProcessor(processor)
    }

    /**
     * Toggles the flashlight if available
     */
    fun toggleFlashlight(enabled: Boolean) {
        cameraSource.toggleFlashlight(enabled)
    }

    /**
     * Sets the focusMode on the Camera, IF AND ONLY IF it is supported by the camera.
     */
    fun setFocusMode(focusMode: String) {
        cameraSource.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO)
    }

}