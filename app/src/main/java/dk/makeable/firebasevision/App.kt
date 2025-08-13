package dk.makeable.firebasevision

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (FirebaseApp.getApps(this).isEmpty()) {
            val options = FirebaseOptions.fromResource(this)
            if (options != null) {
                FirebaseApp.initializeApp(this, options)
            } else {
                // Fallback to default initialization if resources are not found
                FirebaseApp.initializeApp(this)
            }
        }
    }
}
