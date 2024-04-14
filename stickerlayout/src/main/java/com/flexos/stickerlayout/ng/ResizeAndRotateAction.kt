package com.flexos.stickerlayout.ng

import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.View.OnTouchListener
import com.flexos.virtualcommon.widget.TargetParamsScope
import com.flexos.virtualcommon.widget.TransformScope
import kotlin.math.abs
import kotlin.math.sqrt

class ResizeAndRotateAction(
    private val withTransform: (TransformScope.() -> Unit) -> Unit,
    private val withTargetParams: (TargetParamsScope.() -> Unit) -> Unit,
) : OnTouchListener {
    companion object {
        const val TAG = "ResizeAndRotateAction"
    }

    private var targetInitialWidth = 0F
    private var targetInitialHeight = 0F
    private var viewInitialDiagonal = 0F
    private var targetInitialTranX = 0F
    private var targetInitialTranY = 0F
    private var targetInitialCenterX = 0F
    private var targetInitialCenterY = 0F
    private var targetInitialRotate = 0F
    private var downAngle = 0F

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            ACTION_DOWN -> {
                withTargetParams {
                    targetInitialWidth = width.toFloat()
                    targetInitialHeight = height.toFloat()
                    targetInitialTranY = translationY
                    targetInitialTranX = translationX
                    targetInitialRotate = rotate
                    targetInitialCenterX = left + (width / 2F)
                    targetInitialCenterY = top + (height / 2F)

                    // 后续的大小比例,都是基于按下时的大小变的
                    val downWidth = abs(v.left + event.x - targetInitialCenterX) // 由于是从中心点开始算起
                    val downHeight = abs(v.top + event.y - targetInitialCenterY)
                    viewInitialDiagonal = sqrt(downWidth * downWidth + downHeight * downHeight) // 缩放后的对角线长度

                    downAngle = event.absAngle(
                        targetInitialCenterX,
                        targetInitialCenterY,
                        v.left.toFloat(),
                        v.top.toFloat()
                    )
                    Log.d(
                        TAG,
                        "onTouch down(${event.x} ${event.y}) w/h($targetInitialWidth,$targetInitialHeight) touch:w/h($downWidth,$downHeight) center:($targetInitialCenterX,$targetInitialCenterY) angle:$downAngle"
                    )
                }
            }

            ACTION_MOVE -> {
                val nextWidth = abs(v.left + event.x - targetInitialCenterX) // 由于是从中心点开始算起 所以需要x2
                val nextHeight = abs(v.top + event.y - targetInitialCenterY)
                val nextDiagonal =  sqrt(nextWidth * nextWidth + nextHeight * nextHeight) // 缩放后的对角线长度
                val ratio = nextDiagonal / viewInitialDiagonal // 计算他们的比例
                val adjustedWidth = targetInitialWidth * ratio
                val adjustedHeight = targetInitialHeight * ratio


                val newAngle = event.absAngle(
                    targetInitialCenterX,
                    targetInitialCenterY,
                    v.left.toFloat(),
                    v.top.toFloat()
                )

                withTransform {
//                    resize(adjustedWidth.toInt(), adjustedHeight.toInt())
                    rotate(targetInitialRotate + (downAngle - newAngle))
                    /*translation(
                        targetInitialTranX + (adjustedWidth - targetInitialWidth) * -0.5F,
                        targetInitialTranY + (adjustedHeight - targetInitialHeight) * -0.5F
                    )*/
                }

                Log.d(
                    TAG,
                    "onTouch move(${event.x} ${event.y}) w/h($targetInitialWidth,$targetInitialHeight) touch:w/h($nextWidth,$nextHeight) center:($targetInitialCenterX,$targetInitialCenterY) angle:$newAngle"
                )
            }

            ACTION_UP -> {

            }
        }
        return true
    }


    /*override fun onDown(v: View, ev: MotionEvent): Boolean {

        downAngle = ev.absAngle(viewInitialCenterX, viewInitialCenterY)

        Log.d(
            TAG,
            "onDown:${ev.x} ${ev.y} raw:${ev.rawX} ${ev.rawY} center:$viewInitialCenterX $viewInitialCenterY"
        )
        return true
    }

    override fun onMove(
        v: View, ev: MotionEvent, viewInitialX: Float, viewInitialY: Float, dx: Float, dy: Float
    ) {
        Log.d(EditLayout.TAG, "onMove:${ev.x} ${ev.y} raw:${ev.rawX} ${ev.rawY}")
        val nextWidth = abs(ev.rawX - viewInitialCenterX) * 2f // 由于是从中心点开始算起 所以需要x2
        val nextHeight = abs(ev.rawY - viewInitialCenterY) * 2f


        val ratio = nextDiagonal / viewInitialDiagonal // 计算他们的比例
        val adjustedWidth = viewInitialWidth * ratio
        val adjustedHeight = viewInitialHeight * ratio

        val curAngle = ev.absAngle(viewInitialCenterX, viewInitialCenterY)

        withTransform {
            resize(adjustedWidth.toInt(), adjustedHeight.toInt())
            rotate(viewInitialRotate + (downAngle - curAngle))
            translation(
                viewInitialTranX + (adjustedWidth - viewInitialWidth) * -0.5F,
                viewInitialTranY + (adjustedHeight - viewInitialHeight) * -0.5F
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

    }*/
}