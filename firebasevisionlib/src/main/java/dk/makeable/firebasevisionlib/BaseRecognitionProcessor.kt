package dk.makeable.firebasevisionlib

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.common.FirebaseMLException
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

abstract class RecognitionProcessorListener<in ResultType> {
    abstract fun didRecognizeResult(result: ResultType, processor: RecognitionProcessor)
}

abstract class BaseRecognitionProcessor<DetectionType: Any, ResultType: Any, DetectorType: Any>(private var listener: RecognitionProcessorListener<ResultType>? = null): RecognitionProcessor {

    companion object {
        private val TAG = "TextRecProc"
    }

    private var detector: DetectorType = getDetector()

    // Whether we should ignore process(). This is usually caused by feeding input data faster than
    // the model can handle.
    private val shouldThrottle = AtomicBoolean(false)

    //region ----- Exposed Methods -----

    fun setListener(l: RecognitionProcessorListener<ResultType>) {
        this.listener = l
    }

    override fun stop() {
        try {
            stopDetector(detector)
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Text Detector: $e")
        }
    }


    @Throws(FirebaseMLException::class)
    override fun process(data: ByteBuffer, frameMetadata: FrameMetadata, graphicOverlay: GraphicOverlay) {

        if (shouldThrottle.get()) {
            return
        }
        val metadata = FirebaseVisionImageMetadata.Builder()
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            .setWidth(frameMetadata.width)
            .setHeight(frameMetadata.height)
            .setRotation(frameMetadata.rotation)
            .build()

        detectInVisionImage(FirebaseVisionImage.fromByteBuffer(data, metadata), frameMetadata, graphicOverlay)
    }

    //endregion

    //region ----- Helper Methods -----
    private fun onSuccess(result: DetectionType, frameMetadata: FrameMetadata, graphicOverlay: GraphicOverlay) {
        // Notify listener of results, if any
        getResult(result)?.let { result ->
            listener?.didRecognizeResult(result, this)
        }

        // Call the drawGraphicOverLayWithResults if there were any results
        populateGraphicOverlay(result, frameMetadata, graphicOverlay)
    }

    private fun detectInVisionImage(
        image: FirebaseVisionImage,
        metadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {

        detectResults(image, detector)
            .addOnSuccessListener { result ->
                shouldThrottle.set(false)
                this.onSuccess(result, metadata, graphicOverlay)
            }
            .addOnFailureListener { e ->
                shouldThrottle.set(false)
                this.onFailure(e)
            }
        // Begin throttling until this frame of input has been processed, either in onSuccess or
        // onFailure.
        shouldThrottle.set(true)
    }
    //endregion

    //region ----- Abstract Methods -----
    /**
     * A call to this funtion should stop the detector, for example by calling detector.stopDetection(), detector.close() or something similar.
     */
    abstract fun stopDetector(detector: DetectorType)

    /**
     * A call to this function should supply the detector with the passed image, letting the detector return a task of its results.
     */
    abstract fun detectResults(image: FirebaseVisionImage, detector: DetectorType): Task<DetectionType>

    /**
     * Called after results have been found, and a graphical overlay should be drawn. This is where you would add GraphicOverlay components to the UI, could be a TextGraphic or a BoundingBoxGraphic for example, or any other custom overlay class.
     */
    abstract fun populateGraphicOverlay(result: DetectionType, frameMetadata: FrameMetadata, graphicOverlay: GraphicOverlay)

    /**
     * Called before sending the results out to the listener of this recognizer.
     */
    abstract fun getResult(detectionResults: DetectionType): ResultType?

    /**
     * Called whenever an error happens in this RecognitionProcessor.
     */
    abstract fun onFailure(e: Exception)

    /**
     * Returns the detector that will be used in this Processor to find results.
     */
    abstract fun getDetector(): DetectorType
    //endregion

}