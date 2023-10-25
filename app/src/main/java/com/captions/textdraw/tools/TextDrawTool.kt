package com.captions.textdraw.tools

import android.graphics.PointF
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import com.captions.textdraw.models.Segment
import kotlin.math.atan2
import kotlin.math.sqrt

@OptIn(ExperimentalTextApi::class)
class TextDrawTool {

    companion object {
        const val DEBUG_TEXT = "Text Brush "
    }

    // set this text to draw it
    var textToDraw: String = DEBUG_TEXT

    var fontSize = 40.sp

    var textMeasurer: TextMeasurer? = null

    // section will represent a line between two draw points
    private var sectionDistance = 10 // amount of pixels needed to drag to consider adding a new point in a segment // TODO: should scale with font size
    private var letterSize = 4 // amount of sections needed to draw a letter // TODO: should scale with font size

    private var currentCharIdx = 0 // represents the index of the next letter we need to draw

    private var isDragging: Boolean = false

    private val segments: MutableList<Segment> = mutableListOf()

    private var currentSectionDistance = 0f // represents the current length of a section

    private fun distance(p1: PointF, p2: PointF): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        return sqrt(dx * dx + dy * dy)
    }

    private fun getCurrentSegment(): Segment {
        if (segments.isEmpty()) {
            segments.add(Segment()) // create a new empty segment
        }

        return segments.last()
    }

    fun onDragStarted(position: PointF) {
        isDragging = true
        segments.add(Segment()) // create a new segment
    }

    fun onDragEvent(currentPosition: PointF, prevPosition: PointF) {
        val dist = distance(currentPosition, prevPosition)
        currentSectionDistance += dist

        // if we are over a section distance then create a new section
        if (currentSectionDistance > sectionDistance) {
            getCurrentSegment().addPoint(currentPosition)
            currentSectionDistance = 0f // reset section distance
        }
    }

    fun onDragStopped() {
        isDragging = false
    }

    fun onDraw(drawScope: DrawScope) = with(drawScope) {
        if (segments.isEmpty()) return@with

        // redraw all segments
        segments.forEach {
            currentCharIdx = 0

            it.points.forEachIndexed { index, point ->
                if (index == 0) return@forEachIndexed

                // after we have passed the set number of points, draw the letter
                if (index % letterSize == 0) {
                    val section = it.points.subList(index - letterSize, index)
                    drawText(drawScope, section)
                }
            }
        }
    }

    @OptIn(ExperimentalTextApi::class)
    private fun drawText(drawScope: DrawScope, section: List<PointF>) = with(drawScope) {

        // get the average position of the last number of points
        val avgX = section.sumOf { it.x.toDouble() } / section.size
        val avgY = section.sumOf { it.y.toDouble() } / section.size
        val avgPosition = PointF(avgX.toFloat(), avgY.toFloat())

        val charToDraw = textToDraw[currentCharIdx].toString()

        val measuredText = textMeasurer?.measure(
            text = charToDraw,
            style = TextStyle(
                fontSize = fontSize,
                color = Color.White
            ),
        )

        // we rotate the letter based on the average degree value of set number of lines which the user dragged
        val avgDegrees = calculateAvgDegree(section.take(2)) // last 2 points works best

        rotate(
            degrees = avgDegrees,
            pivot = avgPosition.toOffset()
        ) {
            measuredText?.let { drawText(it, topLeft = avgPosition.toOffset()) }
        }

        currentCharIdx += 1

        // reset the index to zero if we reached the end of the text so it draws the text again
        if (currentCharIdx >= textToDraw.length)
            currentCharIdx = 0
    }

    fun onClear() {
        currentCharIdx = 0
        segments.clear()
    }

    fun onUndo() {
        currentCharIdx = 0
        if (segments.isNotEmpty()) segments.removeLast()
    }

    private fun calculateAvgDegree(section: List<PointF>): Float {
        var avgDegree = 0f
        section.forEachIndexed { index, point ->
            if (index == 0) return@forEachIndexed
            avgDegree = angleWithXAxis(section[index - 1], section[index])
        }

        // n points make n - 1 lines
        avgDegree /= (section.size - 1)

        return avgDegree
    }

    /**
     * Debug method that helps to draw actual line where the text will be written.
     */
    private fun drawSegments(drawScope: DrawScope) = with(drawScope) {
        segments.forEach { segment ->
            segment.points.forEachIndexed { index, point ->
                if (index == 0) return@forEachIndexed
                val lastPoint = segment.points[index - 1]
                drawLine(Color.Red, point.toOffset(), lastPoint.toOffset(), strokeWidth = 5f)
            }
        }
    }

    private fun angleWithXAxis(p1: PointF, p2: PointF): Float {
        val deltaY = p2.y - p1.y
        val deltaX = p2.x - p1.x
        val radians = atan2(deltaY, deltaX).toDouble()
        return Math.toDegrees(radians).toFloat()
    }
}

fun PointF.toOffset() = Offset(this.x, this.y)