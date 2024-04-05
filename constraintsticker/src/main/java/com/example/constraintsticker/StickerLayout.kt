package com.example.constraintsticker

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintHelper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.Constraint
import androidx.transition.TransitionManager
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * 虚拟相机根图层,添加到其中的view均自动支持手动layout(由用户通过界面按钮控制)
 */
class StickerLayout(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private var focusedViewId: Int = NO_ID

    private lateinit var constraintSet: ConstraintSetKt

    companion object {
        const val TAG = "StickerLayout"
    }

    override fun onViewAdded(view: View) {
        check(view.id != NO_ID) { " Sticker child must have a id" }
        super.onViewAdded(view)
        setupView(view)
    }

    /**
     * 安装view的行为,比如设置监听 用于进入或退出编辑模式.
     */
    private fun setupView(view: View) {
        if (view is ConstraintHelper) {

        } else if (view is Action) {
            when (view.id) {
                R.id.action_delete -> view.setOnClickListener {
                    onActionDelete(focusedViewId)
                }

                R.id.action_pin -> {
                    view.setOnClickListener {
                        onActionPin(focusedViewId)
                    }
                }

                R.id.action_move -> {
                    view.setOnTouchListener(MoveAction())
                }

                R.id.action_resize -> view.setOnTouchListener(ResizeAndRotateAction())
                else -> {
                    Log.d(TAG, "onViewAdded: ignore $view")
                }
            }
        } else {
            view.setOnClickListener {
                if (focusedViewId == it.id) {
                    exitEditMode()
                } else {
                    enterEditMode(it.id)
                }
            }
        }
    }

    private fun exitEditMode() {
        focusedViewId = NO_ID
        withConstraint {
            setVisibility(R.id.action_widget_pack, GONE)
        }
    }

    private fun withConstraint(anim: Boolean = false, update: ConstraintSet.() -> Unit) {
        constraintSet.update()
        if (anim) TransitionManager.beginDelayedTransition(this)
        constraintSet.applyTo(this)
    }

    private fun enterEditMode(view: Int) {
        exitEditMode()
        focusedViewId = view
        attachActionView(focusedViewId)
        setOnClickListener {
            exitEditMode()
        }
    }

    private fun attachActionView(viewId: Int) {
        if (viewId != NO_ID) {
            constraintSet.withConstraint(viewId) {
                val viewRadius = (layout.diagonal / 2).toInt() // 对角线长度的一半
                withChild(viewId) {
                    mathCircleAngle(
                        leftTop = {
                            Log.d(TAG, "mathCircle leftTop $it")
                            constraintSet.constrainCircle(
                                R.id.action_delete,
                                viewId,
                                viewRadius,
                                it
                            )
                        },
                        rightTop = {
                            Log.d(TAG, "mathCircle rightTop $it")
                            constraintSet.constrainCircle(
                                R.id.action_pin,
                                viewId,
                                viewRadius,
                                it
                            )
                        },
                        rightBottom = {
                            Log.d(TAG, "mathCircle rightBottom $it")
                            constraintSet.constrainCircle(
                                R.id.action_resize,
                                viewId,
                                viewRadius,
                                it
                            )
                        }
                    )
                }
                // 正上方
                constraintSet.constrainCircle(
                    R.id.action_move,
                    viewId,
                    layout.mHeight / 2,
                    0f
                )
                constraintSet.setTranslation(
                    R.id.action_widget_pack,
                    transform.translationX,
                    transform.translationY
                )
                constraintSet.setRotation(
                    R.id.action_widget_pack,
                    transform.rotation
                )
                constraintSet.setVisibility(R.id.action_widget_pack, VISIBLE)
                constraintSet.applyTo(this@StickerLayout)
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        constraintSet = ConstraintSetKt(this)
    }

    /**
     * 置顶这个view
     */
    private fun onActionPin(view: Int) {
        findViewById<View>(view).bringToFront()
        findViewById<ViewSet>(R.id.action_widget_pack).bringToFront()
    }

    private fun onActionDelete(viewId: Int) {
        exitEditMode()
        removeView(findViewById(viewId))
    }


    inner class MoveAction: RelativeTouchListener() {
        private var viewInitialTranslationX = 0F
        private var viewInitialTranslationY = 0F
        private var targetConstraint: Constraint? = null

        override fun onDown(v: View, ev: MotionEvent): Boolean {
            targetConstraint = constraintSet.getConstraint(focusedViewId)
            viewInitialTranslationX = targetConstraint!!.transform.translationX
            viewInitialTranslationY = targetConstraint!!.transform.translationY
            return true
        }

        override fun onMove(
            v: View,
            ev: MotionEvent,
            viewInitialX: Float,
            viewInitialY: Float,
            dx: Float,
            dy: Float
        ) {
            withConstraint {
                setTranslation(
                    R.id.action_widget_pack,
                    viewInitialTranslationX + dx,
                    viewInitialTranslationY + dy
                )
                setTranslation(
                    focusedViewId,
                    viewInitialTranslationX + dx,
                    viewInitialTranslationY + dy
                )
            }
        }

        override fun onUp(
            v: View,
            ev: MotionEvent,
            viewInitialX: Float,
            viewInitialY: Float,
            dx: Float,
            dy: Float,
            velX: Float,
            velY: Float
        ) {
            viewInitialTranslationX = 0F
            viewInitialTranslationY = 0F
            targetConstraint = null
        }
    }


    inner class ResizeAndRotateAction : RelativeTouchListener() {
        private var viewInitialWidth = 0F
        private var viewInitialHeight = 0F
        private var viewInitialRadius = 0F
        private var targetConstraint: Constraint? = null

        override fun onDown(v: View, ev: MotionEvent): Boolean {
            targetConstraint = constraintSet.getConstraint(focusedViewId)
            viewInitialWidth = targetConstraint!!.layout.mWidth.toFloat()
            viewInitialHeight = targetConstraint!!.layout.mHeight.toFloat()
            viewInitialRadius = sqrt(viewInitialWidth * viewInitialWidth + viewInitialHeight * viewInitialHeight)
            return true
        }

        override fun onMove(
            v: View,
            ev: MotionEvent,
            viewInitialX: Float,
            viewInitialY: Float,
            dx: Float,
            dy: Float
        ) {
            val nextWidth = viewInitialWidth + dx // 缩放后的矩形宽度
            val nextHeight = viewInitialHeight + dy // 缩放后的矩形高度
            val nextDistance = sqrt(nextWidth * nextWidth + nextHeight * nextHeight) // 缩放后的对角线长度

            val ra = nextDistance / viewInitialRadius // 计算他们的比例
            withConstraint {// 将对角线比例应用到长宽中
                constrainHeight(focusedViewId, (viewInitialHeight * ra).toInt())
                constrainWidth(focusedViewId, (viewInitialWidth * ra).toInt())
            }
            // 操作按钮也需要跟着动. TODO 和上面的操作 合并到一次事件中 优化性能
            attachActionView(focusedViewId)
        }

        override fun onUp(
            v: View,
            ev: MotionEvent,
            viewInitialX: Float,
            viewInitialY: Float,
            dx: Float,
            dy: Float,
            velX: Float,
            velY: Float
        ) {
            viewInitialHeight = 0F
            viewInitialWidth = 0F
            targetConstraint = null
        }
    }
}


/**
 * 标准化角度
 */
private fun normalizeAngle(angle: Float): Float {
    var normalizedAngle = angle
    while (normalizedAngle < 0) {
        normalizedAngle += 360
    }
    return (normalizedAngle + 90) % 360
}

private fun ViewGroup.withChild(id: Int, action: View.() -> Unit) {
    findViewById<View>(id).action()
}

/**
 * 计算 view每个角相对View中心Y轴顺时针的角度
 */
private fun View.mathCircleAngle(
    leftTop: (Float) -> Unit = {},
    rightTop: (Float) -> Unit = {},
    leftBottom: (Float) -> Unit = {},
    rightBottom: (Float) -> Unit = {}
) {
    val centerX = (left + right) / 2
    val centerY = (top + bottom) / 2

    leftTop(
        normalizeAngle(
            (Math.toDegrees(
                atan2(
                    (top - centerY).toDouble(),
                    (left - centerX).toDouble()
                )
            )).toFloat()
        )
    )
    rightTop(
        normalizeAngle(
            (Math.toDegrees(
                atan2(
                    (top - centerY).toDouble(),
                    (right - centerX).toDouble()
                )
            )).toFloat()
        )
    )
    leftBottom(
        normalizeAngle(
            (Math.toDegrees(
                atan2(
                    (bottom - centerY).toDouble(),
                    (left - centerX).toDouble()
                )
            )).toFloat()
        )
    )

    rightBottom(
        normalizeAngle(
            (Math.toDegrees(
                atan2(
                    (bottom - centerY).toDouble(),
                    (right - centerX).toDouble()
                )
            )).toFloat()
        )
    )
}


