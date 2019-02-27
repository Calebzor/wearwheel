package com.littlesunsoftware.wearwheel

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.spinner_message.view.*

class SpinnerView : ConstraintLayout {
    private lateinit var animator : ValueAnimator
    private lateinit var img : ImageView
    private lateinit var message : TextView
    private lateinit var rotateAnimation: Animation

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        LayoutInflater.from(context).inflate(R.layout.spinner_message, this, true)
        img = findViewById<ImageView>(R.id.spinner)

        rotateAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate)

        message = findViewById(R.id.txtSpinnerMessage)
    }

    fun startSpinnning() {
        img.startAnimation(rotateAnimation)
    }

    fun stopSpinning() {
        img.clearAnimation()
    }

    fun setMessage(msg : String) {
        txtSpinnerMessage.text = msg
    }
}
