package dk.makeable.firebasevisionlib

// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.util.Log
import android.view.*
import java.io.IOException
import kotlin.math.roundToInt


/** Preview the camera image in the screen.  */
class CameraSourcePreview(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {
    private val surfaceView: SurfaceView
    private var startRequested: Boolean = false
    private var surfaceAvailable: Boolean = false
    private var cameraSource: CameraSource? = null

    private var overlay: GraphicOverlay? = null

    private var zoomListener: ((zoom: Int) -> Unit)? = null

    private var hasPendingZoomLevelAdjustment = false
    private var pendingZoomLevel: Int? = null

    private var readyListener: (() -> Unit)? = null

    // For handling pinch zoom
    private var mDist = 0f

    private val isPortraitMode: Boolean
        get() {
            val orientation = context.resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return false
            }
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return true
            }

            Log.d(TAG, "isPortraitMode returning false by default")
            return false
        }

    init {
        startRequested = false
        surfaceAvailable = false

        surfaceView = SurfaceView(context)
        surfaceView.holder.addCallback(SurfaceCallback())
        addView(surfaceView)
    }

    @Throws(IOException::class)
    fun start(cameraSource: CameraSource?) {
        if (cameraSource == null) {
            stop()
        }

        this.cameraSource = cameraSource

        // As the cameraSource changed, request a layout pass that uses its preview size to measure this view
        requestLayout()

        if (this.cameraSource != null) {
            startRequested = true
            startIfReady()
        }
    }

    @Throws(IOException::class)
    fun start(cameraSource: CameraSource, overlay: GraphicOverlay) {
        this.overlay = overlay
        start(cameraSource)
    }

    fun stop() {
        if (cameraSource != null) {
            cameraSource!!.stop()
        }
    }

    fun release() {
        if (cameraSource != null) {
            cameraSource!!.release()
            cameraSource = null
        }
    }

    @SuppressLint("MissingPermission")
    @Throws(IOException::class)
    private fun startIfReady() {
        if (startRequested && surfaceAvailable) {
            cameraSource!!.start(surfaceView.holder)
            if (overlay != null) {
                val size = cameraSource!!.previewSize!!
                val min = Math.min(size.width, size.height)
                val max = Math.max(size.width, size.height)
                if (isPortraitMode) {
                    // Swap width and height sizes when in portrait, since it will be rotated by
                    // 90 degrees
                    overlay!!.setCameraInfo(min, max, cameraSource!!.cameraFacing)
                } else {
                    overlay!!.setCameraInfo(max, min, cameraSource!!.cameraFacing)
                }
                overlay!!.clear()

                // Ready for params
                readyListener?.invoke()
            }
            startRequested = false

//            adjustZoomLevelIfNeeded()
        }
    }

//    private fun adjustZoomLevelIfNeeded() {
//        // Run a zoom adjustment, if any is pending
//        Log.d(TAG, "adjustZoomLevelIfNeeded called")
//        if (hasPendingZoomLevelAdjustment && pendingZoomLevel != null) {
//            try {
//                setZoomLevel(pendingZoomLevel!!)
//                hasPendingZoomLevelAdjustment = false
//                pendingZoomLevel = null
//            } catch (e: Throwable) {e.printStackTrace()}
//        }
//    }

    private inner class SurfaceCallback : SurfaceHolder.Callback {
        override fun surfaceCreated(surface: SurfaceHolder) {
            surfaceAvailable = true
            try {
                startIfReady()
//                adjustZoomLevelIfNeeded()
            } catch (e: IOException) {
                Log.d(TAG, "Could not start camera source.", e)
            }

        }

        override fun surfaceDestroyed(surface: SurfaceHolder) {
            surfaceAvailable = false
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            Log.d(TAG, "Surface changed: <format: $format, width: $width, height: $height>")
//            adjustZoomLevelIfNeeded()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = View.resolveSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = View.resolveSize(suggestedMinimumHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)

        Log.d(TAG, "CameraSource: ${cameraSource}, PreviewSize: ${cameraSource?.previewSize}")
        cameraSource?.previewSize?.let { previewSize ->
            val ratio = if (previewSize.height >= previewSize.width) previewSize.height.toFloat() / previewSize.width.toFloat()
                else previewSize.width.toFloat() / previewSize.height.toFloat()

            val camHeight = (width * ratio).toInt().toFloat()
            val newCamHeight: Float
            val newHeightRatio: Float

            if (camHeight < height) {
                newHeightRatio = height.toFloat() / previewSize.height.toFloat()
                newCamHeight = newHeightRatio * camHeight
                Log.d(TAG, "$camHeight $height ${previewSize.height} $newHeightRatio $newCamHeight")
                setMeasuredDimension((width * newHeightRatio).toInt(), newCamHeight.toInt())
                Log.d(TAG, "${previewSize.width} | ${previewSize.height} | ration - $ratio | H_ratio - $newHeightRatio | A_width - ${width * newHeightRatio} | A_height - $newCamHeight")
            } else {
                newCamHeight = camHeight
                setMeasuredDimension(width, newCamHeight.toInt())
                Log.d(TAG, "${previewSize.width} | ${previewSize.height} | ratio - $ratio | A_width - $width | A_height - $newCamHeight")
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        var width = 320
        var height = 240
        if (cameraSource != null) {
            val size = cameraSource!!.previewSize
            if (size != null) {
                width = size.width
                height = size.height
            }
        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode) {
            val tmp = width
            width = height
            height = tmp
        }

        val layoutWidth = right - left
        val layoutHeight = bottom - top

        // Computes height and width for potentially doing fit width.
        val childWidth: Int
        val childHeight: Int
        var childXOffset = 0
        var childYOffset = 0
        val widthRatio = layoutWidth.toFloat() / width.toFloat()
        val heightRatio = layoutHeight.toFloat() / height.toFloat()

        // To fill the view with the camera preview, while also preserving the correct aspect ratio,
        // it is usually necessary to slightly oversize the child and to crop off portions along one
        // of the dimensions.  We scale up based on the dimension requiring the most correction, and
        // compute a crop offset for the other dimension.
        if (widthRatio > heightRatio) {
            childWidth = layoutWidth
            childHeight = (height.toFloat() * widthRatio).toInt()
            childYOffset = (childHeight - layoutHeight) / 2
        } else {
            childWidth = (width.toFloat() * heightRatio).toInt()
            childHeight = layoutHeight
            childXOffset = (childWidth - layoutWidth) / 2
        }

        for (i in 0 until childCount) {
            // One dimension will be cropped.  We shift child over or up by this offset and adjust
            // the size to maintain the proper aspect ratio.
            getChildAt(i).layout(
                -1 * childXOffset, -1 * childYOffset,
                childWidth - childXOffset, childHeight - childYOffset
            )
        }

        try {
            startIfReady()
        } catch (e: IOException) {
            Log.d(TAG, "Could not start camera source.", e)
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Get the pointer ID
//        val params = mCamera.getParameters()
        val action = event.action


        if (event.pointerCount > 1) { // Multi touch
            // handle multi-touch events
            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                mDist = getFingerSpacing(event)
            } else if (action == MotionEvent.ACTION_MOVE && cameraSource?.isZoomSupported() == true) {
                handleZoom(event)
            }
        } /*else {
            // handle single touch events
            if (action == MotionEvent.ACTION_UP) {
                handleFocus(event)
            }
        }*/
        return true
    }

    /**
     * Calling this function will register a function which will be called when the zoom level changes. Returns the zoom level as an integer from 0% (no zoom) to 100% (max zoom)
     */
    public fun setZoomListener(zoomListener: (zoom: Int) -> Unit) {
        this.zoomListener = zoomListener
    }

    /**
     * Calling this function will set the zoom level of the camera preview.
     */
    @Throws(RuntimeException::class)
    public fun setZoomLevel(zoomLevel: Int) {
//        if (cameraSource == null) { // If the cameraSource has not yet been attached, make sure to schedule a call to set the zoom when it has been attached.
//            scheduleZoomLevelForWhenAttachhed(zoomLevel)
//            throw RuntimeException() // Makes sure that either the cameraSource was not null, and therefore maybe throwed something, or throws when scheduling.
//        }
        cameraSource?.setZoomLevel(zoomLevel)
    }

    public fun setOnReadyListener(readyListener: () -> Unit) {
        this.readyListener = readyListener
    }

//    private fun scheduleZoomLevelForWhenAttachhed(zoomLevel: Int) {
//        this.hasPendingZoomLevelAdjustment = true
//        this.pendingZoomLevel = zoomLevel
//    }

    private fun handleZoom(event: MotionEvent) {
        val maxZoom = cameraSource?.maxZoom()!!
        var zoom = cameraSource?.zoom()!!
        val newDist = getFingerSpacing(event)
        if (newDist > mDist) {
            //zoom in
            if (zoom < maxZoom)
                zoom++
        } else if (newDist < mDist) {
            //zoom out
            if (zoom > 0)
                zoom--
        }
        mDist = newDist
        try {
            cameraSource?.setZoomLevel(zoom)
        } catch (e: Exception) {e.printStackTrace()}
        Log.d(TAG, "New zoom level: $zoom")
        val zoomPercentage = (zoom.toDouble()/maxZoom.toDouble()) * 100 // Ex. 25/50 = 0.5
        zoomListener?.invoke(zoomPercentage.roundToInt())
    }

//    fun handleFocus(event: MotionEvent) {
//        val pointerId = event.getPointerId(0)
//        val pointerIndex = event.findPointerIndex(pointerId)
//        // Get the pointer's current position
//        val x = event.getX(pointerIndex)
//        val y = event.getY(pointerIndex)
//
//        val supportedFocusModes = params.getSupportedFocusModes()
//        if (supportedFocusModes != null && supportedFocusModes!!.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
//            mCamera.autoFocus(object : Camera.AutoFocusCallback() {
//                fun onAutoFocus(b: Boolean, camera: Camera) {
//                    // currently set to auto-focus on single touch
//                }
//            })
//        }
//    }

    /** Determine the space between the first two fingers  */
    private fun getFingerSpacing(event: MotionEvent): Float {
        // ...
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    companion object {
        private val TAG = "FIREBASEVISION:PREVIEW"
    }
}
