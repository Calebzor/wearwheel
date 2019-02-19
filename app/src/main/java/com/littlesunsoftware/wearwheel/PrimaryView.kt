package com.littlesunsoftware.wearwheel

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.littlesunsoftware.wearwheel.events.EventHub
import com.littlesunsoftware.wearwheel.events.EventListener
import com.littlesunsoftware.wearwheel.events.EventType

import java.util.Date
import kotlin.math.roundToInt

class PrimaryView : LinearLayout {
    private var mBatteryLevel: Int = 0
    private var mSpeed: Float = 0.toFloat()
    private var mClock = Date()
    private var mTemperature: Int = 0
    private var mCurrent: Float = 0.toFloat()
    private var mVoltage: Float = 0.toFloat()
    private var mTime: Long = 0
    private var mDistance: Float = 0.0f

    private lateinit var speedArcMeter: ArcMeter
    private lateinit var speedText: TwoDigitDisplay
    private lateinit var BatteryImageView: BatteryImageView
    private lateinit var clockText: TextView
    private lateinit var spinnerView: SpinnerView
    private lateinit var primaryBG: ImageView

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
        mBatteryLevel = 80
        mSpeed = 35.4f
        mTemperature = 34
        mCurrent = 5.2f
        mVoltage = 62.3f

        LayoutInflater.from(context).inflate(R.layout.primary, this, true)

        speedText = findViewById(R.id.txtSpeed)
        BatteryImageView = findViewById(R.id.battery)
        clockText = findViewById(R.id.txtClock)
        speedArcMeter = findViewById(R.id.speedArcMeter)
        spinnerView = findViewById(R.id.primarySpinner)
        primaryBG = findViewById(R.id.primaryBG)

        (findViewById(R.id.imgHorn) as ImageView).apply {
            setOnClickListener {
                var a = context as SpeedometerActivity
                a.playHorn()
            }
        }

        (findViewById(R.id.txtSpeedUnits) as TextView).apply {
            text = Units().getRateLabel()
        }

        setSpeed(0f)
        setClock(Date())
        showSpinner()

        EventHub.instance.addListener(EventListener("primaryAmbient", EventType.AmbientDisplay){
            primaryBG.visibility = if (it.getBoolean("ambient", false)) View.INVISIBLE else View.VISIBLE
        })
    }

    fun showSpinner() {
        spinnerView.apply {
            setMessage("Connecting...")
            visibility = View.VISIBLE
            startSpinnning()
        }
    }

    fun hideSpinner() {
        spinnerView.apply {
            visibility = View.INVISIBLE
            stopSpinning()
        }
    }

    fun refresh() {
        invalidate()
        requestLayout()
    }

    fun setMaxSpeed(maxSpeedLocalUnits : Float) {
        speedArcMeter.maxValue = maxSpeedLocalUnits.roundToInt()
    }

    fun setVoltage(volts: Float) {
        this.mVoltage = volts
        EventHub.instance.post(EventType.Volts, Bundle().apply { putFloat("volts", volts) })
    }

    fun setSpeed(kphSpeed: Float) {
        var speed = Units().getRate(kphSpeed)

        this.mSpeed = speed
        speedArcMeter.currValue = speed
        speedText.digit = Math.round(speed)
    }

    fun setCurrent(amps: Float) {
        this.mCurrent = amps
        EventHub.instance.post(EventType.Amps, Bundle().apply { putFloat("amps", amps) })
    }

    fun setBatteryLevel(batteryLevel: Int) {
        this.mBatteryLevel = batteryLevel
        BatteryImageView.setBatteryLevel(batteryLevel)
    }

    fun setTemp(tempC: Float) {
        val temp = Units().getTemperature(tempC)
        this.mTemperature = temp.toInt()
        EventHub.instance.post(EventType.Degrees, Bundle().apply { putFloat("degrees", temp) })
    }

    fun setClock(clock: Date) {
        this.mClock = clock
        clockText.text = DateFormat.format("h:mm", clock).toString()
    }

    fun setDuration(time: Long) {
        this.mTime = time
        EventHub.instance.post(EventType.Time, Bundle().apply { putLong("time", time) })
    }

    fun setDistance(distanceMeters: Float) {
        this.mDistance = Units().getDistance(distanceMeters / 1000f)
        EventHub.instance.post(EventType.Distance, Bundle().apply { putFloat("distance", mDistance) })
    }
}
