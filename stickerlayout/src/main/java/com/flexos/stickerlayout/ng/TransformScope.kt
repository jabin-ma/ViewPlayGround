package com.flexos.virtualcommon.widget

import android.graphics.Point
import android.graphics.Rect
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet


interface TransformScope {
    fun translation(x: Float, y: Float)
    fun scale(x: Float, y: Float)
    fun rotate(angle: Float)

    fun resize(width: Int, height: Int)
}


interface TargetParamsScope {
    val width: Int
        get() = 0
    val height: Int
        get() = 0
    val translationX: Float
        get() = -1F
    val translationY: Float
        get() = -1F
    val rotate: Float
        get() = -1F
    val left:Int
        get() = 1
    val top:Int
        get() = 1
    val right:Int
        get() = 1
    val bottom:Int
        get() = 1
}

class ConstraintTargetParamsScope(private val view: View, private val constraint: ConstraintSet.Constraint) :
    TargetParamsScope {

    override val width: Int
        get() = constraint.layout.mWidth
    override val height: Int
        get() = constraint.layout.mHeight
    override val rotate: Float
        get() = constraint.transform.rotation
    override val translationX: Float
        get() = constraint.transform.translationX
    override val translationY: Float
        get() = constraint.transform.translationY
    override val left: Int
        get() = view.left
    override val top: Int
        get() = view.top
    override val right: Int
        get() = view.right
    override val bottom: Int
        get() = view.bottom
}

fun <T : ConstraintSet> transformWithSet(
    target: T, targetViews: IntArray,
    transFormBegin: () -> Unit,
    transForm: TransformSetScope.() -> Unit,
    transFormEnd: (T) -> Unit
) {
    transFormBegin()
    TransformSetScope(target, targetViews).transForm()
    transFormEnd(target)
}

class TransformSetScope(private val constraintSet: ConstraintSet, private val targets: IntArray) :
    TransformScope {
    override fun translation(x: Float, y: Float) {
        targets.forEach {
            constraintSet.setTranslation(it, x, y)
        }
    }

    override fun resize(width: Int, height: Int) {
        targets.forEach {
            constraintSet.constrainHeight(it, height)
            constraintSet.constrainWidth(it, width)
            constraintSet.setTransformPivot(it, width / 2F, height / 2F)
        }
    }

    override fun scale(x: Float, y: Float) {
        targets.forEach {
            constraintSet.setScaleX(it, x)
            constraintSet.setScaleY(it, y)
        }
    }

    override fun rotate(angle: Float) {
        targets.forEach {
            constraintSet.setRotation(it, angle)
        }
    }
}
