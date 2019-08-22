package dk.makeable.firebasevisionlib

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

// --------------- Activity ---------------
fun FragmentActivity.setupVisionDetection(
    graphicOverlay: GraphicOverlay,
    cameraSourcePreview: CameraSourcePreview,
    recognitionProcessor: RecognitionProcessor,
    cameraPermissionRationaleString: String,
    cameraPermissionDeniedString: String
): FirebaseVisionSetup<*> {

    val setup = FirebaseVisionSetup(
        this,
        this,
        graphicOverlay,
        cameraSourcePreview,
        recognitionProcessor,
        cameraPermissionRationaleString,
        cameraPermissionDeniedString
    )

    VisionSetupManager.instance.registerVisionSetup(setup) // The setup itself will make sure to unregister when needed.

    return setup
}

fun Fragment.setupVisionDetection(
    graphicOverlay: GraphicOverlay,
    cameraSourcePreview: CameraSourcePreview,
    recognitionProcessor: RecognitionProcessor,
    cameraPermissionRationaleString: String,
    cameraPermissionDeniedString: String
): FirebaseVisionSetup<*> {

    val setup = FirebaseVisionSetup(
        this, requireContext(),
        graphicOverlay,
        cameraSourcePreview,
        recognitionProcessor,
        cameraPermissionRationaleString,
        cameraPermissionDeniedString
    )

    VisionSetupManager.instance.registerVisionSetup(setup) // The setup itself will make sure to unregister when needed.

    return setup
}


// --------------- Fragment ---------------