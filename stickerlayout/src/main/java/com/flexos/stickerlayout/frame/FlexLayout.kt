package com.flexos.stickerlayout.frame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.flexos.stickerlayout.R

class FlexLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val helperMap = mutableMapOf<Int, ActionPanel>()

    init {
        setWillNotDraw(false)
    }

    private var editorController: ActionPanel? = null

    override fun onViewAdded(child: View) {
        check(child.id != NO_ID)
        with(child.layoutParams) {
            check(width != MATCH_PARENT && height != MATCH_PARENT && width != WRAP_CONTENT && height != WRAP_CONTENT)
            helperMap[child.id] = ActionPanel(child, moveX = 100F, moveY = 300F)
        }

        child.setOnClickListener {
            Log.d("TAG", "setOnClickListener: ")
            if (editorController == null) {
                editorController = helperMap[it.id]
            } else {
                editorController = null
            }
            invalidate()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (childCount == 0) return
        helperMap.forEach { (key, value) -> value.apply() }
    }

    val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
    }

    private val drawableSize = 80


    override fun onDrawForeground(canvas: Canvas) {

        editorController?.run {
            canvas.drawRoundRect(postRect, 10f, 10f, paint)
            drawActions(canvas)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return editorController?.run {
            care(ev.x.toInt(), ev.y.toInt())
        } ?: super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return editorController?.run {
            onTouchEvent(event)
        } ?: super.onTouchEvent(event)
    }
}

class Action(
    private val drawable: Drawable,
    private val doLayout: Rect.(Int, Int, Int, Int, Int, Int) -> Unit,
    private val doTouch: (MotionEvent) -> Boolean = { false },
) {
    fun contains(x: Int, y: Int) = drawable.bounds.contains(x, y)

    fun layout(l: Int, t: Int, r: Int, b: Int, cx: Int, cy: Int) {
        drawable.bounds.apply {
            doLayout(l, t, r, b, cx, cy)
        }
    }

    fun draw(canvas: Canvas) = drawable.draw(canvas)
    fun onTouchEvent(event: MotionEvent): Boolean {
        return doTouch(event)
    }
}

class ActionPanel(
    private val base: View,
    l: Int = 0,
    t: Int = 0,
    r: Int = base.layoutParams.width,
    b: Int = base.layoutParams.height,
    scaleX: Float = 1F,
    scaleY: Float = 1F,
    moveX: Float = 0F,
    moveY: Float = 0F
) {
    private val rawRect = RectF()
    val postRect = RectF()
    private val matrix = Matrix()

    private var scaleX = 1F
    private var scaleY = 1F
    private var moveX = 0F
    private var moveY = 0F

    private val drawableSize = 80

    private val actionList = listOf(
        Action(base.resources.getDrawable(R.drawable.ic_close, null), { l, t, _, _, _, _ ->
            left = l
            top = t
            right = l + drawableSize
            bottom = t + drawableSize
        }),
        Action(base.resources.getDrawable(R.drawable.ic_drag, null), { l, t, r, b, cx, cy ->
            left = cx - drawableSize / 2
            top = t
            right = cx - drawableSize / 2 + drawableSize
            bottom = t + drawableSize

        }),
        Action(base.resources.getDrawable(R.drawable.ic_resize, null), { l, t, r, b, cx, cy ->
            left = r
            top = b
            right = r + drawableSize
            bottom = b + drawableSize
        }),
        Action(base.resources.getDrawable(R.drawable.ic_toplevel, null), { l, t, r, b, cx, cy ->
            left = r
            top = t
            right = r + drawableSize
            bottom = t + drawableSize
        })
    )

    val l: Int
        get() {
            return postRect.left.toInt()
        }
    val t: Int
        get() {
            return postRect.top.toInt()
        }
    val r: Int
        get() {
            return postRect.right.toInt()
        }
    val b: Int
        get() {
            return postRect.bottom.toInt()
        }
    val w: Int
        get() {
            return r - l
        }
    val h: Int
        get() {
            return b - t
        }

    init {
        rawRect.set(l.toFloat(), t.toFloat(), r.toFloat(), b.toFloat())
        scale(scaleX, scaleY)
        move(moveX, moveY)
        updateMatrix()
    }

    fun scale(sx: Float, sy: Float) {
        scaleX = sx
        scaleY = sy
        updateMatrix()
    }

    fun move(x: Float, y: Float) {
        moveX = x
        moveY = y
        updateMatrix()
    }

    private fun updateMatrix() {
        matrix.reset()
        matrix.preScale(scaleX, scaleY, rawRect.width() / 2F, rawRect.height() / 2F)
        matrix.postTranslate(moveX, moveY)
        matrix.mapRect(postRect, rawRect)
    }

    fun apply() {
        Log.d("TAG", "apply: $rawRect $postRect")
        base.layout(l, t, r, b)
    }

    fun care(x: Int, y: Int) = actionList.any {
        it.contains(x, y)
    }

    fun drawActions(canvas: Canvas) {
        val padding = (drawableSize * 0.5).toInt()
        val left = l - padding
        val top = t - padding
        val right = r - (drawableSize - padding)
        val bottom = b - (drawableSize - padding)
        val centerX = l + w / 2
        val centerY = t + h / 2
        actionList.forEach {
            it.layout(left, top, right, bottom, centerX, centerY)
            it.draw(canvas)
        }
    }

    private var targetAction: Action? = null
    fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            ACTION_DOWN -> {
                targetAction = actionList.firstOrNull { it.contains(ev.x.toInt(), ev.y.toInt()) }
            }
            ACTION_UP -> {
                targetAction = null
            }
        }
        return targetAction?.onTouchEvent(ev) ?: false
    }
}