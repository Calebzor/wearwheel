package com.littlesunsoftware.wearwheel

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.ImageView
import android.graphics.DashPathEffect
import android.os.Build
import com.littlesunsoftware.wearwheel.events.EventHub
import com.littlesunsoftware.wearwheel.events.EventListener
import com.littlesunsoftware.wearwheel.events.EventType

class ArcMeter(context: Context, attributeSet: AttributeSet) : ImageView(context, attributeSet) {
    val a = context.obtainStyledAttributes(attributeSet, R.styleable.ArcMeter)
    var background: Bitmap? = null
    val paint: Paint = Paint()
    val arcPadding = a.getDimension(R.styleable.ArcMeter_arc_padding, SpeedometerActivity.dipToPx(5f))

    val meterArcPaint = Paint().apply {
        isAntiAlias = true
        color = a.getColor(R.styleable.ArcMeter_arc_color, Color.BLUE)
        strokeWidth = a.getDimensionPixelSize(R.styleable.ArcMeter_arc_width, 10).toFloat()
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    val tickPaint = Paint().apply {
        isAntiAlias = true
        color = a.getColor(R.styleable.ArcMeter_tick_color, Color.BLUE)
        strokeWidth = a.getDimensionPixelSize(R.styleable.ArcMeter_tick_width, 10).toFloat()
        xfermode = PorterDuffXfermode(PorterDuff.Mode.XOR)
    }
    val tickLength = a.getDimensionPixelSize(R.styleable.ArcMeter_tick_length, 10).toFloat()

    val labelPaint= Paint().apply {
        isAntiAlias = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = context.resources.getFont(R.font.roboto_regular)
        }
        color = tickPaint.color
        textSize = a.getDimensionPixelSize(R.styleable.ArcMeter_tick_label_size, 10).toFloat()
    }

    val labelPadding = a.getDimensionPixelSize(R.styleable.ArcMeter_tick_label_padding, 10).toFloat()

    val valueArcPaint = Paint().apply {
        isAntiAlias = true
        color = a.getColor(R.styleable.ArcMeter_value_color, Color.BLUE)
        strokeWidth = a.getDimensionPixelSize(R.styleable.ArcMeter_arc_width, 10).toFloat() // * 0.75f
        style = Paint.Style.STROKE
        //strokeCap = Paint.Cap.ROUND

        val dashPath = DashPathEffect(floatArrayOf(SpeedometerActivity.dipToPx(20.55f), SpeedometerActivity.dipToPx(1f)), 1.0.toFloat())
        pathEffect = dashPath
    }

    val arcStartAngle : Float
            get() = if (SpeedometerActivity.ChinSize > 0f) 125f else 90f

    val arcSweepAngle : Float
            get() = 270f - (arcStartAngle - 90f)

    var maxValue = 22
        set(value) {
            field = value
            if (width > 0 && height > 0)
                makeBackground(width, height)
        }

    var _currValue = 10F
    var currValue
        get() = _currValue
        set(value) {
            _currValue = value
            invalidate()
        }

    var ambientDisplay = false

    fun init() {
        EventHub.instance.addListener(EventListener("arcMeterAmbient", EventType.AmbientDisplay){
            ambientDisplay = it.getBoolean("ambient", false)
            invalidate()
        })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventHub.instance.removeListener("arcMeterAmbient")
    }

    override fun onDraw(canvas: Canvas?) {
        if (!ambientDisplay) {
            super.onDraw(canvas)

            canvas?.drawBitmap(this.background, 0F, 0F, paint)
        }

        drawValueArc(canvas!!)
    }

    private fun drawValueArc(canvas: Canvas) {
        var targetRect = RectF(0F, 0F, width.toFloat(), height.toFloat())
        targetRect.inset((meterArcPaint.strokeWidth / 2f) + arcPadding,  (meterArcPaint.strokeWidth / 2f) + arcPadding)

        val degPerValue = arcSweepAngle / maxValue

        canvas.drawArc(targetRect, arcStartAngle, degPerValue * currValue, false, valueArcPaint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        if (w > 0) {
            init() // Hack

            makeBackground(w.toInt(), h.toInt())

            valueArcPaint.shader = SweepGradient(
                w.toFloat() / 2F,
                h.toFloat() / 2F,
                intArrayOf(
                    context.getColor(R.color.batteryChargingColor),
                    context.getColor(R.color.batteryChargingColor),
                    context.getColor(R.color.batteryLowColor),
                    context.getColor(R.color.batteryEmptyColor)
                ),
                floatArrayOf(0F, 0.7F, 0.9F, 1F)
            )
        }
    }
    
    private fun makeBackground(width: Int, height: Int) {
        background?.recycle()
        background = null

        background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(background)

        makeArc(canvas, width, height)

        val degValue = makeTicks(canvas, width, height)

        makeLabels(canvas, width, height, degValue)
    }
    
    private fun makeArc(canvas:Canvas, width: Int, height: Int) {
        var targetRect = RectF(0F, 0F, width.toFloat(), height.toFloat())
        targetRect.inset((meterArcPaint.strokeWidth / 2f) + arcPadding,  (meterArcPaint.strokeWidth / 2f) + arcPadding)

        canvas.drawArc(targetRect, arcStartAngle, arcSweepAngle, false, meterArcPaint)
    }

    private fun makeTicks(canvas: Canvas, width: Int, height: Int): ArrayList<Pair<Float, Int>> {
        val midPt = PointF(this.width.toFloat() / 2.0f, this.height.toFloat() / 2.0f)
        val degPerValue = arcSweepAngle / maxValue
        val tickEvery = 5
        val numTicks = (maxValue / tickEvery) + 1
        val degPerTick = tickEvery * degPerValue


        var degValue = ArrayList<Pair<Float, Int>>()

        var value = 0
        var angle = arcStartAngle
        canvas.save()
        canvas.rotate(arcStartAngle, midPt.x, midPt.y)
        for (i in 0..numTicks) {
            canvas.drawLine(this.width.toFloat() - arcPadding, midPt.y, this.width.toFloat() - tickLength - arcPadding, midPt.y, tickPaint)
            degValue.add(Pair(angle, value))

            value += 5

            if (value <= maxValue) {
                canvas.rotate(degPerTick, midPt.x, midPt.y)
                angle += degPerTick
            }
            else {
                canvas.rotate((arcStartAngle + arcSweepAngle) - angle, midPt.x, midPt.y)
                angle = arcStartAngle + arcSweepAngle
            }
        }
        canvas.restore()

        return degValue
    }

    private fun makeLabels(canvas: Canvas, width: Int, height: Int, degValue: ArrayList<Pair<Float, Int>>) {
        for (ds in degValue) {
            val deg = ds.first
            val text = ds.second.toString()
            val textRadius = Math.max(labelPaint.measureText(text), labelPaint.textSize) / 2F
            val textPoint = floatArrayOf(width.toFloat() - (textRadius + tickLength + labelPadding) - arcPadding, height / 2.0f)
            val matrix = Matrix()
            matrix.postRotate(deg,width / 2.0f, height / 2.0f)
            matrix.mapPoints(textPoint)

            canvas.drawText(text, textPoint[0] - textRadius, textPoint[1] + (labelPaint.textSize / 2F), labelPaint)
        }
    }
}