package com.captions.textdraw.models

import android.graphics.PointF

data class Segment(val points: MutableList<PointF> = mutableListOf()) {

    fun addPoint(point: PointF) {
        points.add(point)
    }
}