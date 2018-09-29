package com.dino.googlemapflutter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

import com.google.android.gms.maps.SupportMapFragment

class TouchSupportMapFragment : SupportMapFragment() {
    var mOriginalContentView: View? = null
    public lateinit var mTouchView: TouchableWrapper
    private var mListener: NonConsumingTouchListener? = null

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? {
        mOriginalContentView = super.onCreateView(inflater, parent, savedInstanceState)
        mTouchView = TouchableWrapper(this.context!!)
        mTouchView.addView(mOriginalContentView)
        return mTouchView
    }

    override fun getView(): View? {
        return mOriginalContentView
    }

    fun setNonConsumingTouchListener(listener: NonConsumingTouchListener) {
        mListener = listener
    }

    interface NonConsumingTouchListener {
        fun onTouch(motionEvent: MotionEvent): Boolean
    }

    inner class TouchableWrapper(context: Context) : FrameLayout(context) {
        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            mListener?.onTouch(event)

            return super.dispatchTouchEvent(event)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            mListener?.onTouch(event)

            return super.onTouchEvent(event)
        }
    }
}