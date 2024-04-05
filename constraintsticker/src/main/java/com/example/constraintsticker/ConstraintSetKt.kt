package com.example.constraintsticker

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

class ConstraintSetKt(private val layout: ConstraintLayout) : ConstraintSet() {
    init {
        clone(layout)
    }

    fun apply() {
        applyTo(layout)
    }
}

fun ConstraintSetKt.withConstraint(id:Int , action: ConstraintSet.Constraint.() -> Unit) {
    getConstraint(id).action()
}

fun ConstraintSetKt.beginTransaction(action: ConstraintSet.() -> Unit) {
    action()
    apply()
}