package dk.makeable.firebasevisionlib

import java.nio.ByteBuffer

/**
 * Represents a processor for recognizing results via ML Kit from Firebase.
 */
interface RecognitionProcessor {

    fun stop()

    fun process(data: ByteBuffer, frameMetadata: FrameMetadata, graphicOverlay: GraphicOverlay)

}