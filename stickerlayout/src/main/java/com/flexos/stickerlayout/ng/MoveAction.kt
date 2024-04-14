package com.flexos.virtualcommon.widget

import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener

class MoveAction(
    private val withTransform: (TransformScope.() -> Unit) -> Unit,
    private val withTargetParams: (TargetParamsScope.() -> Unit) -> Unit,
) : OnTouchListener {

    private var viewInitialTranslationX = 0F
    private var viewInitialTranslationY = 0F
    private val downPoint = PointF()


    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                withTargetParams {
                    viewInitialTranslationX = translationX
                    viewInitialTranslationY = translationY
                }
                downPoint.x = event.x
                downPoint.y = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                withTransform {
                    translation(
                        viewInitialTranslationX + (event.x - downPoint.x),
                        viewInitialTranslationY + (event.y - downPoint.y)
                    )
                }
            }

            MotionEvent.ACTION_UP -> {

            }
        }
        return true
    }
}