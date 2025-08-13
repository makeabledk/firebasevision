package dk.makeable.firebasevision

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer
import dk.makeable.firebasevisionlib.BaseRecognitionProcessor
import dk.makeable.firebasevisionlib.FrameMetadata
import dk.makeable.firebasevisionlib.GraphicOverlay
import dk.makeable.firebasevisionlib.TextGraphic

class TextRecognitionProcessor :
    BaseRecognitionProcessor<FirebaseVisionText, FirebaseVisionText, FirebaseVisionTextRecognizer>() {

    override fun stopDetector(detector: FirebaseVisionTextRecognizer) {
        detector.close()
    }

    override fun detectResults(
        image: FirebaseVisionImage,
        detector: FirebaseVisionTextRecognizer
    ): Task<FirebaseVisionText> {
        return detector.processImage(image)
    }

    override fun populateGraphicOverlay(
        result: FirebaseVisionText,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        graphicOverlay.clear()
        for (block in result.textBlocks) {
            for (line in block.lines) {
                for (element in line.elements) {
                    graphicOverlay.add(TextGraphic(graphicOverlay, element))
                }
            }
        }
    }

    override fun getResult(detectionResults: FirebaseVisionText): FirebaseVisionText? {
        return detectionResults
    }

    override fun onFailure(e: Exception) {
        Log.e("TextRecProc", "Text recognition failed", e)
    }

    override fun getDetector(): FirebaseVisionTextRecognizer {
        return FirebaseVision.getInstance().onDeviceTextRecognizer
    }
}
