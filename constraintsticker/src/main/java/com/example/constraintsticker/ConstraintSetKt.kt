package com.example.constraintsticker

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import kotlin.math.pow
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