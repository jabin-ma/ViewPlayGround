package com.flexos.stickerlayout.ng

import android.view.MotionEvent
import kotlin.math.atan2

fun MotionEvent.absAngle(x: Float, y: Float, offsetX: Float = 0F, offsetY: Float = 0F): Float {
    return ((Math.toDegrees(
        atan2(
            x - (offsetX + this.x), y - (offsetY + this.y)
        ).toDouble()
    ) + 360) % 360).toFloat()
}