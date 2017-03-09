package com.igalata.bubblepicker.rendering

import android.content.Context
import android.opengl.GLSurfaceView
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.MotionEvent
import com.igalata.bubblepicker.BubblePickerListener
import com.igalata.bubblepicker.BubbleSize
import com.igalata.bubblepicker.R
import com.igalata.bubblepicker.exception.EmptyPickerException
import com.igalata.bubblepicker.model.Color
import com.igalata.bubblepicker.model.PickerItem
import java.util.*

/**
 * Created by irinagalata on 1/19/17.
 */
class BubblePicker : GLSurfaceView {

    @ColorInt var background: Int = 0
        set(value) {
            field = value
            renderer.backgroundColor = Color(value)
        }
    var items: ArrayList<PickerItem>? = null
        set(value) {
            field = value
            renderer.items = value ?: ArrayList()
        }
    var maxSelectedCount: Int? = null
        set(value) {
            renderer.maxSelectedCount = value
        }
    var listener: BubblePickerListener? = null
        set(value) {
            renderer.listener = value
        }
    var bubbleSize = BubbleSize.MEDIUM
        set(value) {
            if (value >= BubbleSize.SMALL && value <= BubbleSize.LARGE) {
                renderer.bubbleSize = value
            }
        }
    val selectedItems: List<PickerItem?>
        get() = renderer.selectedItems

    private val renderer = PickerRenderer(this)
    private var startX = 0f
    private var startY = 0f
    private var previousX = 0f
    private var previousY = 0f

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
        attrs?.let { retrieveAttrubutes(attrs) }
    }

    override fun onResume() {
        super.onResume()
        if (items?.isEmpty() ?: false) throw EmptyPickerException()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                previousX = event.x
                previousY = event.y
            }
            MotionEvent.ACTION_UP -> {
                if (isClick(event)) renderer.resize(event.x, event.y)
                renderer.release()
            }
            MotionEvent.ACTION_MOVE -> {
                if (isSwipe(event)) {
                    renderer.swipe(event.x, event.y)
                    previousX = event.x
                    previousY = event.y
                } else {
                    release()
                }
            }
            else -> release()
        }

        return true
    }

    private fun release() = postDelayed({ renderer.release() }, 1000)

    private fun isClick(event: MotionEvent) = Math.abs(event.x - startX) < 20 && Math.abs(event.y - startY) < 20

    private fun isSwipe(event: MotionEvent) = Math.abs(event.x - previousX) > 20 && Math.abs(event.y - previousY) > 20

    private fun retrieveAttrubutes(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.BubblePicker)

        if (array.hasValue(R.styleable.BubblePicker_maxSelectedCount)) {
            maxSelectedCount = array.getInt(R.styleable.BubblePicker_maxSelectedCount, -1)
        }

        if (array.hasValue(R.styleable.BubblePicker_backgroundColor)) {
            background = array.getColor(R.styleable.BubblePicker_backgroundColor, -1)
        }

        array.recycle()
    }

}