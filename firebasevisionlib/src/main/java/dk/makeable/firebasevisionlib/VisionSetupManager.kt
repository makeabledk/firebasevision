package dk.makeable.firebasevisionlib

class VisionSetupManager private constructor() {

    private val setups: MutableSet<FirebaseVisionSetup<*>> = hashSetOf() // Set prevents duplicates

    private object Holder {
        val INSTANCE = VisionSetupManager()
    }

    companion object {
        val instance: VisionSetupManager by lazy { Holder.INSTANCE }
    }

    fun registerVisionSetup(setup: FirebaseVisionSetup<*>) {
        setups.add(setup)
    }

    fun unregisterVisionSetup(setup: FirebaseVisionSetup<*>) {
        setups.remove(setup)
    }

}