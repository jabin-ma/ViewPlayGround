package com.flexos.stickerlayout

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintHelper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.R
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * 用于集中管理一组View, 例如统一缩放、移动、旋转等,其自身Z轴低于被管理的View
 */
class ActionPanel : ConstraintHelper {
    private var mRotationCenterX = Float.NaN
    private var mRotationCenterY = Float.NaN
    private var mGroupRotateAngle = Float.NaN
    var mContainer: ConstraintLayout? = null
    private var mScaleX = 1f
    private var mScaleY = 1f
    protected var mComputedCenterX = Float.NaN
    protected var mComputedCenterY = Float.NaN
    protected var mComputedMaxX = Float.NaN
    protected var mComputedMaxY = Float.NaN
    protected var mComputedMinX = Float.NaN
    protected var mComputedMinY = Float.NaN
    var mNeedBounds = true
    var mViews: Array<View?>? = null // used to reduce the getViewById() cost
    private var mShiftX = 0f
    private var mShiftY = 0f
    private var mApplyVisibilityOnAttach = false
    private var mApplyElevationOnAttach = false

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    /**
     * @param attrs
     */
    @SuppressLint("CustomViewStyleable")
    override fun init(attrs: AttributeSet) {
        super.init(attrs)
        mUseViewMeasure = false
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.ConstraintLayout_Layout
        )
        val n = a.getIndexCount()
        for (i in 0 until n) {
            val attr = a.getIndex(i)
            if (attr == R.styleable.ConstraintLayout_Layout_android_visibility) {
                mApplyVisibilityOnAttach = true
            } else if (attr == R.styleable.ConstraintLayout_Layout_android_elevation) {
                mApplyElevationOnAttach = true
            }
        }
        a.recycle()
        setWillNotDraw(true)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mContainer = parent as ConstraintLayout
        if (mApplyVisibilityOnAttach || mApplyElevationOnAttach) {
            val visibility = visibility
            val elevation: Float = elevation
            for (i in 0 until mCount) {
                val id = mIds[i]
                val view = mContainer!!.getViewById(id)
                if (view != null) {
                    if (mApplyVisibilityOnAttach) {
                        view.visibility = visibility
                    }
                    if (mApplyElevationOnAttach) {
                        if (elevation > 0
                        ) {
                            view.translationZ += elevation
                        }
                    }
                }
            }
        }
    }

    /**
     * @param container
     */
    override fun updatePreDraw(container: ConstraintLayout) {
        mContainer = container
        val rotate = rotation
        if (rotate == 0f) {
            if (!java.lang.Float.isNaN(mGroupRotateAngle)) {
                mGroupRotateAngle = rotate
            }
        } else {
            mGroupRotateAngle = rotate
        }
    }

    /**
     * Rotates all associated views around a single point post layout..
     * The point is the middle of the bounding box or set by setPivotX,setPivotX;
     * @param angle
     */
    override fun setRotation(angle: Float) {
        mGroupRotateAngle = angle
        transform()
    }

    /**
     * Scales all associated views around a single point post layout..
     * The point is the middle of the bounding box or set by setPivotX,setPivotX;
     * @param scaleX The value to scale in X.
     */
    override fun setScaleX(scaleX: Float) {
        mScaleX = scaleX
        transform()
    }

    /**
     * Scales all associated views around a single point post layout..
     * The point is the middle of the bounding box or set by setPivotX,setPivotX;
     * @param scaleY The value to scale in X.
     */
    override fun setScaleY(scaleY: Float) {
        mScaleY = scaleY
        transform()
    }

    /**
     * Sets the pivot point for scale operations.
     * Setting it to Float.NaN (default) results in the center of the group being used.
     * @param pivotX The X location of the pivot point
     */
    override fun setPivotX(pivotX: Float) {
        mRotationCenterX = pivotX
        transform()
    }

    /**
     * Sets the pivot point for scale operations.
     * Setting it to Float.NaN (default) results in the center of the group being used.
     * @param pivotY The Y location of the pivot point
     */
    override fun setPivotY(pivotY: Float) {
        mRotationCenterY = pivotY
        transform()
    }

    /**
     * Shift all the views in the X direction post layout.
     * @param dx number of pixes to shift
     */
    override fun setTranslationX(dx: Float) {
        mShiftX = dx
        transform()
    }

    /**
     * Shift all the views in the Y direction post layout.
     * @param dy number of pixes to shift
     */
    override fun setTranslationY(dy: Float) {
        mShiftY = dy
        transform()
    }

    /**
     *
     */
    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        applyLayoutFeatures()
    }

    /**
     *
     */
    override fun setElevation(elevation: Float) {
        super.setElevation(elevation)
        applyLayoutFeatures()
    }

    /**
     * @param container
     */
    override fun updatePostLayout(container: ConstraintLayout) {
        reCacheViews()
        mComputedCenterX = Float.NaN
        mComputedCenterY = Float.NaN
        val params = layoutParams as ConstraintLayout.LayoutParams
        val widget = params.constraintWidget
        widget.setWidth(0)
        widget.setHeight(0)
        calcCenters()
        val left = mComputedMinX.toInt() + paddingLeft
        val top = mComputedMinY.toInt() + paddingTop
        val right = mComputedMaxX.toInt() - paddingRight
        val bottom = mComputedMaxY.toInt() - paddingBottom
        layout(left, top, right, bottom)
        transform()
    }

    override fun bringToFront() {
        super.bringToFront()
        getViews(mContainer).forEach {
            it.bringToFront()
        }
    }


    private fun reCacheViews() {
        if (mContainer == null) {
            return
        }
        if (mCount == 0) {
            return
        }
        if (mViews == null || mViews!!.size != mCount) {
            mViews = arrayOfNulls(mCount)
        }
        for (i in 0 until mCount) {
            val id = mIds[i]
            mViews!![i] = mContainer!!.getViewById(id)
        }
    }

    protected fun calcCenters() {
        if (mContainer == null) {
            return
        }
        if (!mNeedBounds) {
            if (!(java.lang.Float.isNaN(mComputedCenterX) || java.lang.Float.isNaN(mComputedCenterY))) {
                return
            }
        }
        if (java.lang.Float.isNaN(mRotationCenterX) || java.lang.Float.isNaN(mRotationCenterY)) {
            val views = getViews(mContainer)
            var minx = views[0].left
            var miny = views[0].top
            var maxx = views[0].right
            var maxy = views[0].bottom
            for (i in 0 until mCount) {
                val view = views[i]
                minx = min(minx.toDouble(), view.left.toDouble()).toInt()
                miny = min(miny.toDouble(), view.top.toDouble()).toInt()
                maxx = max(maxx.toDouble(), view.right.toDouble()).toInt()
                maxy = max(maxy.toDouble(), view.bottom.toDouble()).toInt()
            }
            mComputedMaxX = maxx.toFloat()
            mComputedMaxY = maxy.toFloat()
            mComputedMinX = minx.toFloat()
            mComputedMinY = miny.toFloat()
            mComputedCenterX = if (java.lang.Float.isNaN(mRotationCenterX)) {
                ((minx + maxx) / 2).toFloat()
            } else {
                mRotationCenterX
            }
            mComputedCenterY = if (java.lang.Float.isNaN(mRotationCenterY)) {
                ((miny + maxy) / 2).toFloat()
            } else {
                mRotationCenterY
            }
        } else {
            mComputedCenterY = mRotationCenterY
            mComputedCenterX = mRotationCenterX
        }
    }

    private fun transform() {
        if (mContainer == null) {
            return
        }
        if (mViews == null) {
            reCacheViews()
        }
        calcCenters()
        val rad =
            if (java.lang.Float.isNaN(mGroupRotateAngle)) 0.0 else Math.toRadians(mGroupRotateAngle.toDouble())
        val sin = sin(rad).toFloat()
        val cos = cos(rad).toFloat()
        val m11 = mScaleX * cos
        val m12 = -mScaleY * sin
        val m21 = mScaleX * sin
        val m22 = mScaleY * cos
        for (i in 0 until mCount) {
            val view = mViews!![i]
            val x = (view!!.left + view.right) / 2
            val y = (view.top + view.bottom) / 2
            val dx = x - mComputedCenterX
            val dy = y - mComputedCenterY
            val shiftx = m11 * dx + m12 * dy - dx + mShiftX
            val shifty = m21 * dx + m22 * dy - dy + mShiftY
            view.translationX = shiftx
            view.translationY = shifty
            view.scaleY = mScaleY
            view.scaleX = mScaleX
            if (!java.lang.Float.isNaN(mGroupRotateAngle)) {
                view.rotation = mGroupRotateAngle
            }
        }
    }

    /**
     *
     * @param container
     */
    override fun applyLayoutFeaturesInConstraintSet(container: ConstraintLayout) {
        applyLayoutFeatures(container)
    }

    companion object {
        private const val TAG = "Region"
    }
}