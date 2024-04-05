package com.example.constraintsticker

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintHelper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Constraints

class ConstraintSticker : ConstraintHelper {

    companion object {
        const val TAG = "ConstraintSticker"
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onDraw(canvas: Canvas) {
        Log.d(TAG, "onDraw: $canvas")
        super.onDraw(canvas)
    }

    override fun onAttachedToWindow() {
        Log.d(TAG, "onAttachedToWindow")
        super.onAttachedToWindow()
    }

    override fun setTag(key: Int, tag: Any?) {
        Log.d(TAG, "setTag $key $tag")
        super.setTag(key, tag)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d(TAG, "onMeasure ")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun init(attrs: AttributeSet?) {
        Log.d(TAG, "init")
        super.init(attrs)
    }

    override fun addView(view: View?) {
        Log.d(TAG, "addView $view")
        super.addView(view)
    }

    override fun removeView(view: View?): Int {
        Log.d(TAG, "removeView $view")
        return super.removeView(view)
    }

    override fun getReferencedIds(): IntArray {
        return super.getReferencedIds()
    }

    override fun setReferencedIds(ids: IntArray?) {
        Log.d(TAG, "setReferencedIds: $ids")
        super.setReferencedIds(ids)
    }

    override fun validateParams() {
        Log.d(TAG, "validateParams")
        super.validateParams()
    }

    override fun setIds(idList: String?) {
        Log.d(TAG, "setIds: $idList")
        super.setIds(idList)
    }

    override fun setReferenceTags(tagList: String?) {
        Log.d(TAG, "setReferenceTags: $tagList")
        super.setReferenceTags(tagList)
    }

    override fun applyLayoutFeatures(container: ConstraintLayout?) {
        Log.d(TAG, "applyLayoutFeatures: $container")
        super.applyLayoutFeatures(container)
    }

    override fun applyLayoutFeatures() {
        Log.d(TAG, "applyLayoutFeatures")
        super.applyLayoutFeatures()
    }

    override fun applyLayoutFeaturesInConstraintSet(container: ConstraintLayout?) {
        super.applyLayoutFeaturesInConstraintSet(container)
    }

    override fun updatePreLayout(container: ConstraintLayout?) {
        Log.d(TAG, "updatePreLayout")
        super.updatePreLayout(container)
    }

    override fun getViews(layout: ConstraintLayout?): Array<View> {
        Log.d(TAG, "getViews")
        return super.getViews(layout)
    }

    override fun updatePostLayout(container: ConstraintLayout?) {
        Log.d(TAG, "updatePostLayout")
        super.updatePostLayout(container)
    }

    override fun updatePostMeasure(container: ConstraintLayout?) {
        Log.d(TAG, "updatePostMeasure")
        super.updatePostMeasure(container)
    }

    override fun updatePostConstraints(container: ConstraintLayout?) {
        Log.d(TAG, "updatePostConstraints")
        super.updatePostConstraints(container)
    }

    override fun updatePreDraw(container: ConstraintLayout?) {
        Log.d(TAG, "updatePreDraw")
        super.updatePreDraw(container)
    }

    override fun containsId(id: Int): Boolean {
        Log.d(TAG, "containsId $id")
        return super.containsId(id)
    }

    override fun indexFromId(id: Int): Int {
        Log.d(TAG, "indexFromId $id")
        return super.indexFromId(id)
    }
}