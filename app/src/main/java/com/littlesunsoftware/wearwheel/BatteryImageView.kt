package com.littlesunsoftware.wearwheel

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import com.littlesunsoftware.wearwheel.events.EventHub
import com.littlesunsoftware.wearwheel.events.EventListener
import com.littlesunsoftware.wearwheel.events.EventType

class BatteryImageView(context: Context, attributeSet: AttributeSet) : ImageView(context, attributeSet) {
    var _batteryLevel: Int = 0
    val _batteryEmptyColor = context.getColor(R.color.batteryEmptyColor)
    val _batteryLowColor = context.getColor(R.color.batteryLowColor)
    val _batteryNormalColor = context.getColor(R.color.batteryNormalColor)

    lateinit var batteryPath: Path
    lateinit var batteryFillPath: Path

    val ambientFGColor = Color.WHITE
    var charging = false

    var ambientDisplay = false

    val textPaint = Paint().apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.dataLight)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = context.resources.getFont(R.font.roboto_regular)
        }
        isAntiAlias = true
        setShadowLayer(SpeedometerActivity.dipToPx(5f), 2f, 2f, Color.BLACK)
    }

    val textPaintAmbient = Paint().apply {
        style = Paint.Style.FILL
        color = ambientFGColor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = context.resources.getFont(R.font.roboto_regular)
        }
        isAntiAlias = false
    }

    val batteryPathPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = _batteryNormalColor
        strokeWidth = SpeedometerActivity.dipToPx(3f)
        isAntiAlias = true
    }

    val batteryPathPaintAmbient = Paint().apply {
        style = Paint.Style.STROKE
        color = ambientFGColor
        strokeWidth = SpeedometerActivity.dipToPx(3f)
        isAntiAlias = false
    }

    val barPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.WHITE
        strokeWidth = batteryPathPaint.strokeWidth
        isAntiAlias = true
    }

    init {
        setBatteryLevel(60)
        scaleType = ScaleType.FIT_XY
        textAlignment = View.TEXT_ALIGNMENT_CENTER

        EventHub.instance.addListener(EventListener("batteryAmbient", EventType.AmbientDisplay){
            ambientDisplay = it.getBoolean("ambient", false)
            setBatteryLevel(_batteryLevel) // Force battery color update
        })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventHub.instance.removeListener("batteryAmbient")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (w > 0 && h > 0) {
            batteryPath = makeBatteryPath()
            batteryFillPath = makeBatteryFillPath()
        }
    }

    fun setBatteryLevel(batteryLevel: Int) {
        _batteryLevel = batteryLevel

        barPaint.color =
            when {
                ambientDisplay -> ambientFGColor
                _batteryLevel <= 10 -> _batteryEmptyColor
                _batteryLevel <= 35 -> _batteryLowColor
                else -> _batteryNormalColor
            }

        batteryPathPaint.color = barPaint.color

        var spSize = 18F
        var scaledSizeInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spSize, getResources().getDisplayMetrics());
        textPaint.textSize = scaledSizeInPixels;
        textPaintAmbient.textSize = textPaint.textSize

        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        val w = width
        val h = height

        if (!ambientDisplay)
            canvas!!.apply {
                save()
                clipRect(RectF(0f, 0f, w * (_batteryLevel / 100f), h.toFloat()))
                canvas!!.drawPath(batteryFillPath, barPaint)
                restore()
            }

        drawTextCenter(canvas!!, if (ambientDisplay) textPaintAmbient else textPaint, "$_batteryLevel%")

        canvas!!.drawPath(batteryPath, if (ambientDisplay) batteryPathPaintAmbient else batteryPathPaint)
    }

    private fun drawTextCenter(canvas: Canvas, paint: Paint, text: String) {
        var r = Rect()
        canvas.getClipBounds(r)
        val cHeight = r.height()
        val cWidth = r.width()
        paint.textAlign = Paint.Align.LEFT
        paint.getTextBounds(text, 0, text.length, r)
        val x = cWidth / 2f - r.width() / 2f - r.left
        val y = cHeight / 2f + r.height() / 2f - r.bottom
        canvas.drawText(text, x, y, paint)
    }

    private fun makeBatteryPath() : Path {
        val w = width.toFloat()
        val h = height.toFloat()
        val rad = SpeedometerActivity.dipToPx(5f)
        val capWidth = rad * 1.5f
        val capTop = h / 4f
        val capBottom = (h / 3f) * 2f
        val capLeft = w - capWidth

        return Path().apply {
            moveTo(rad, 0f)

            lineTo(capLeft - rad, 0f)
            quadTo(capLeft, 0f, capLeft, rad)
            lineTo(capLeft, capTop)
            lineTo(w - rad, capTop)
            quadTo(w, capTop, w, capTop + rad)
            lineTo(w, capBottom - rad)
            quadTo(w, capBottom, w - rad, capBottom)
            lineTo(capLeft, capBottom)
            lineTo(capLeft, h - rad)
            quadTo(capLeft, h, capLeft - rad, h)
            lineTo(rad, h)
            quadTo(0f, h, 0f, h - rad)
            lineTo(0f, rad)
            quadTo(0f, 0f, rad, 0f)
            transform(Matrix().apply {
                preTranslate(batteryPathPaint.strokeWidth / 2f, batteryPathPaint.strokeWidth / 2f)
                postScale(w / (w + batteryPathPaint.strokeWidth), h / (h + batteryPathPaint.strokeWidth))
            })
        }
    }

    private fun makeBatteryFillPath() : Path{
        val w = width.toFloat()
        val h = height.toFloat()

        return makeBatteryPath().apply {
            var sw = batteryPathPaint.strokeWidth + SpeedometerActivity.dipToPx(4f)
            transform(Matrix().apply {
                preTranslate(sw / 2f, sw / 2f)
                postScale(w / (w + sw), h / (h + sw))
            })
        }
    }
}