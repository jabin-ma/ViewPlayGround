package com.flexos.stickerlayout

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import kotlin.math.cos
import kotlin.math.sqrt

class ConstraintSetKt(private val layout: ConstraintLayout) : ConstraintSet() {
    init {
        clone(layout)
    }

    fun apply() {
        applyTo(layout)
    }
}

fun ConstraintSetKt.withConstraint(id: Int, action: ConstraintSet.Constraint.() -> Unit) {
    getConstraint(id).action()
}

fun ConstraintSetKt.beginTransaction(action: ConstraintSetKt.() -> Unit) {
    action()
    apply()
}

/**
 * 对角线长度
 */
val ConstraintSet.Layout.diagonal: Float
    get() = sqrt(1.0F * mWidth * mWidth + mHeight * mHeight)


fun ConstraintSet.Layout.calculateRadius(angle: Int) : Double{
    val absAngle = angle % 45.0
    val loop = angle % 180.0
    val centerX = mWidth / 2
    val centerY = mHeight / 2
    return when (loop) {
        in 135.0..180.0 -> {
            centerY / cos(Math.toRadians(45.0 - absAngle))
        }

        in 90.0..135.0 -> {
            centerX / cos(Math.toRadians(absAngle))
        }

        in 45.0..90.0 -> {
            centerX / cos(Math.toRadians(45.0 - absAngle))
        }

        in 0.0..45.0 -> {
            centerY / cos(Math.toRadians(absAngle))
        }

        else -> {
            -1.0
        }
    }
}