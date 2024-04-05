package com.example.constraintsticker

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintHelper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet.Constraint
import androidx.core.content.res.ResourcesCompat
import kotlin.math.abs
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
        if (focusedViewId != NO_ID) {
            withChild(focusedViewId) {
                foreground = null
            }
            focusedViewId = NO_ID
            beginTransaction {
                setVisibility(R.id.action_widget_pack, GONE)
            }
        }
    }

    private fun beginTransaction(anim: Boolean = false, update: ConstraintSetKt.() -> Unit) {
        constraintSet.beginTransaction(update)
    }

    private fun enterEditMode(view: Int) {
        exitEditMode()
        focusedViewId = view
        withChild(focusedViewId) {
            foreground = ResourcesCompat.getDrawable(resources, R.drawable.bg_frame, null)
        }
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
        constraintSet.apply() // 要重新apply一下 否则位置会不正确
    }

    private fun onActionDelete(viewId: Int) {
        exitEditMode()
        removeView(findViewById(viewId))
    }

    inner class MoveAction : RelativeTouchListener() {
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
            beginTransaction {
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
        private var viewInitialDiagonal = 0F
        private var viewInitialTranX = 0F
        private var viewInitialTranY = 0F
        private var viewOnScreenCenterX = 0F
        private var viewOnScreenCenterY = 0F
        private var viewInitialRotate = 0F
        private var downAngle = 0F

        override fun onDown(v: View, ev: MotionEvent): Boolean {
            constraintSet.withConstraint(focusedViewId) {
                viewInitialWidth = layout.mWidth.toFloat()
                viewInitialHeight = layout.mHeight.toFloat()
                viewInitialDiagonal =
                    sqrt(viewInitialWidth * viewInitialWidth + viewInitialHeight * viewInitialHeight)
                viewInitialTranY = transform.translationY
                viewInitialTranX = transform.translationX
                viewInitialRotate = transform.rotation
            }
            withChild(focusedViewId) {
                // 根据父view的位置计算自身的位置, 不能直接调用自身的getLocationOnScreen,因为会包含变换(缩放旋转等等)
                val parentLocation = IntArray(2)
                this@StickerLayout.getLocationOnScreen(parentLocation)
                viewOnScreenCenterX = parentLocation[0] + x + viewInitialWidth / 2
                viewOnScreenCenterY = parentLocation[1] + y + viewInitialHeight / 2
                downAngle = ev.absAngle(viewOnScreenCenterX, viewOnScreenCenterY)
            }
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
//            val nextWidth = viewInitialWidth + dx * 2 // 缩放后的矩形宽度
//            val nextHeight = viewInitialHeight + dy * 2// 缩放后的矩形高度
            val nextWidth = abs(ev.rawX - viewOnScreenCenterX) * 2
            val nextHeight = abs(ev.rawY - viewOnScreenCenterY) * 2

            val nextDiagonal = sqrt(nextWidth * nextWidth + nextHeight * nextHeight) // 缩放后的对角线长度
            val ratio = nextDiagonal / viewInitialDiagonal // 计算他们的比例
            val adjustedWidth = viewInitialWidth * ratio
            val adjustedHeight = viewInitialHeight * ratio

            val curAngle = ev.absAngle(viewOnScreenCenterX, viewOnScreenCenterY)
            beginTransaction {// 将对角线比例应用到长宽中
                // 沿中心点放大缩小
                withConstraint(focusedViewId) {
                    setTranslation(
                        focusedViewId,
                        viewInitialTranX + (adjustedWidth - viewInitialWidth) * -0.5F,
                        viewInitialTranY + (adjustedHeight - viewInitialHeight) * -0.5F
                    )
                }
                setRotation(focusedViewId, viewInitialRotate + (downAngle - curAngle))
                constrainHeight(focusedViewId, adjustedHeight.toInt())
                constrainWidth(focusedViewId, adjustedWidth.toInt())
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

private fun MotionEvent.absAngle(x: Float, y: Float): Float {
    return ((Math.toDegrees(
        atan2(
            x - rawX, y - rawY
        ).toDouble()
    ) + 360) % 360).toFloat()
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


