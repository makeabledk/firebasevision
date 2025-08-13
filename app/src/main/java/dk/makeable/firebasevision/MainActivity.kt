package dk.makeable.firebasevision

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dk.makeable.firebasevisionlib.CameraSourcePreview
import dk.makeable.firebasevisionlib.FirebaseVisionSetup
import dk.makeable.firebasevisionlib.GraphicOverlay

class MainActivity : AppCompatActivity() {

    private lateinit var cameraPreview: CameraSourcePreview
    private lateinit var graphicOverlay: GraphicOverlay
    private var visionSetup: FirebaseVisionSetup<MainActivity>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraPreview = findViewById(R.id.camera_preview)
        graphicOverlay = findViewById(R.id.graphic_overlay)

        val processor = TextRecognitionProcessor()
        visionSetup = FirebaseVisionSetup(
            owner = this,
            context = this,
            graphicOverlay = graphicOverlay,
            cameraSourcePreview = cameraPreview,
            recognitionProcessor = processor,
            rationaleString = "Camera permission is required to scan text",
            deniedString = "Camera permission denied"
        )
    }
}
