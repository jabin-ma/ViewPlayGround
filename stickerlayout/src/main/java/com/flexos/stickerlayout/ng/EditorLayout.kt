package com.flexos.stickerlayout.ng

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.helper.widget.Layer
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.flexos.stickerlayout.R
import com.flexos.virtualcommon.widget.ConstraintTargetParamsScope
import com.flexos.virtualcommon.widget.transformWithSet
import kotlin.math.atan2
import kotlin.math.sqrt

class Action(context: Context, attrs: AttributeSet) : ImageView(context, attrs)
class ActionPanel(context: Context, attrs: AttributeSet) : Layer(context, attrs)

class EditorLayout(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    val constraintSet: ConstraintSet = ConstraintSet()

    val actionPanelId = R.id.action_widget_pack

    var editingViewId = NO_ID

    override fun onViewAdded(view: View) {
        super.onViewAdded(view)
        when (view::class) {
            Action::class -> {
                setUpAction(view)
            }

            ActionPanel::class -> {}
            else -> {
                view.setOnClickListener {
                    enterEditMode(it.id)
                }
            }
        }
    }

    fun setUpAction(view: View) {
        when (view.id) {
            R.id.action_resize -> {
                view.setOnTouchListener(ResizeAndRotateAction(
                    withTransform = { transForm ->
                        transformWithSet(
                            target = constraintSet,
                            intArrayOf(editingViewId),
                            transFormBegin = {},
                            transForm = transForm,
                            transFormEnd = {
                                linkActionPanel(editingViewId)
                                it.applyTo(this)
                            }
                        )
                    },
                    withTargetParams = {
                        ConstraintTargetParamsScope(
                            findViewById(editingViewId),
                            constraintSet.getConstraint(editingViewId)
                        ).it()
                    }
                ))
            }
        }
    }


    fun enterEditMode(targetId: Int) {
        // 退出之前的编辑模式
        // 把action绑定到当前正在编辑的view上
        linkActionPanel(targetId)
        constraintSet.applyTo(this)
        editingViewId = targetId
    }


    fun exitEditMode() {}


    private fun linkActionPanel(targetId: Int){
        with(constraintSet) set@{
            // 绑定几个按钮的位置
            with(getConstraint(targetId)) view@{
                constrainCircle(
                    R.id.action_delete,
                    targetId,
                    (layout.diagonal / 2).toInt(),
                    layout.angle(0, 0).toFloat()
                )
                constrainCircle(
                    R.id.action_move,
                    targetId,
                    (layout.mHeight / 2),
                    layout.angle(layout.centerX, 0).toFloat()
                )
                constrainCircle(
                    R.id.action_pin,
                    targetId,
                    (layout.diagonal / 2).toInt(),
                    layout.angle(layout.mWidth, 0).toFloat()
                )
                constrainCircle(
                    R.id.action_resize,
                    targetId,
                    (layout.diagonal / 2).toInt(),
                    layout.angle(layout.mWidth, layout.mHeight).toFloat()
                )

                setTranslation(actionPanelId, transform.translationX, transform.translationY)
                setRotation(actionPanelId, transform.rotation)
                // 锚点永远在中心点
                setTransformPivot(actionPanelId, layout.mWidth / 2F, layout.mHeight / 2F)
            }
        }
    }


    override fun onFinishInflate() {
        super.onFinishInflate()
        constraintSet.clone(this)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        constraintSet.clone(this)
    }

    val ConstraintSet.Layout.diagonal: Float get() = sqrt(1.0F * mWidth * mWidth + mHeight * mHeight)
    val ConstraintSet.Layout.centerX get() = mWidth / 2
    val ConstraintSet.Layout.centerY get() = mHeight / 2

    fun ConstraintSet.Layout.angle(x: Int, y: Int) = (Math.toDegrees(
        Math.PI / 2 - atan2(
            (x - centerX).toDouble(),
            (y - centerY).toDouble()
        ) // ConstraintSet的角度是以Y轴为起点的,所以需要在夹角的基础上算
    ) + 360) % 360 + 90
}