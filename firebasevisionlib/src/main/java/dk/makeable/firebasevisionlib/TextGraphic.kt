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

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.google.mlkit.vision.text.Text

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
class TextGraphic(
    overlay: GraphicOverlay,
    private val element: Text.Element?
) : GraphicOverlay.Graphic(overlay) {

    private val rectPaint: Paint = Paint().apply {
        color = MARKER_COLOR
        style = Paint.Style.STROKE
        strokeWidth = STROKE_WIDTH
    }

    private val textPaint: Paint = Paint().apply {
        color = MARKER_COLOR
        textSize = TEXT_SIZE
    }

    init {
        // Redraw the overlay, as this graphic has been added.
        postInvalidate()
    }

    /** Draws the text block annotations for position, size, and raw value on the supplied canvas.  */
    override fun draw(canvas: Canvas) {
        if (element == null) {
            return
        }

        // Draws the bounding box around the TextBlock.
        val rect = RectF(element.boundingBox)
        rect.left = translateX(rect.left)
        rect.top = translateY(rect.top)
        rect.right = translateX(rect.right)
        rect.bottom = translateY(rect.bottom)
        canvas.drawRect(rect, rectPaint)

        // Renders the text at the bottom of the box.
        canvas.drawText(element.text, rect.left, rect.bottom, textPaint)
    }

    companion object {
        private val MARKER_COLOR = Color.GREEN
        private val TEXT_SIZE = 54.0f
        private val STROKE_WIDTH = 4.0f
    }
}
