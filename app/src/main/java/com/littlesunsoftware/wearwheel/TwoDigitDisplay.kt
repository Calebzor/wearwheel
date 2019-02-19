package com.littlesunsoftware.wearwheel

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView

class TwoDigitDisplay : LinearLayout {
    private var digitColor : Int = 0
    private var digitColorEmpty : Int = 0
    lateinit var leftDigit : TextView
    lateinit var rightDigit: TextView

    private var _digit : Int = 0
    var digit
        get() = _digit
        set(value) {
            val leftDigitNumber = (value / 10)
            leftDigit.text = leftDigitNumber.toString()
            leftDigit.setTextColor(if (leftDigitNumber < 1) digitColorEmpty else digitColor)

            var rightDigitNumber = (value % 10)
            rightDigit.text = rightDigitNumber.toString()
            rightDigit.setTextColor(if (rightDigitNumber == 0 && leftDigitNumber == 0) digitColorEmpty else digitColor)
        }

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
        LayoutInflater.from(context).inflate(R.layout.two_digit_display, this, true)

        var attribs = context.obtainStyledAttributes(attrs, R.styleable.TwoDigitDisplay)

        digitColor = attribs.getColor(R.styleable.TwoDigitDisplay_digit_color, Color.WHITE)
        digitColorEmpty = attribs.getColor(R.styleable.TwoDigitDisplay_empty_digit_color, Color.rgb(20, 20, 20))

        var textSize = attribs.getDimensionPixelSize(R.styleable.TwoDigitDisplay_text_size, 10)

        leftDigit = (findViewById(R.id.left_digit) as TextView).apply {
            textSize = textSize
            setTextColor(digitColorEmpty)
        }

        rightDigit = (findViewById(R.id.right_digit) as TextView).apply {
            textSize = textSize
            setTextColor(digitColorEmpty)
        }
    }
}