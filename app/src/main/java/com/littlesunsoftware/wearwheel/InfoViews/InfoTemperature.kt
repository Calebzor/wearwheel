package com.littlesunsoftware.wearwheel.InfoViews

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.littlesunsoftware.wearwheel.R
import com.littlesunsoftware.wearwheel.events.EventHub
import com.littlesunsoftware.wearwheel.events.EventListener
import com.littlesunsoftware.wearwheel.events.EventType
import com.littlesunsoftware.wearwheel.events.ValueAverager
import kotlin.math.roundToInt

class InfoTemperature : LinearLayout {
    private lateinit var txtTemp: TextView
    private lateinit var imgHot: ImageView
    private lateinit var imgCold: ImageView

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
        LayoutInflater.from(context).inflate(R.layout.info_temperature, this, true)
        txtTemp = findViewById(R.id.txtTemperature)
        imgHot = findViewById(R.id.imgHot)
        imgCold = findViewById(R.id.imgCold)


    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)

        if (visibility == View.VISIBLE) {
            attach()
        }
        else {
            detach()
        }
    }

    fun attach() {
        val highTempDetector = ValueAverager(1500L, 140f){
            EventHub.instance.post(EventType.HighTemp, Bundle().apply { putBoolean("hightemp", it) })
        }

        val lowTempDetector = ValueAverager(1500L, 14f){
            EventHub.instance.post(EventType.LowTemp, Bundle().apply { putBoolean("lowemp", it) })
        }

        EventHub.instance.addListener(EventListener("infoviewtemperature", EventType.Degrees) {
            val degreesF = it.getFloat("degrees")
            txtTemp.text = "${degreesF.roundToInt().toString()}°"
            highTempDetector.update(degreesF)
            lowTempDetector.update(degreesF)
        })

        EventHub.instance.addListener(EventListener("infoviewhightemp", EventType.HighTemp){
            imgHot.visibility = if (it.getBoolean("hightemp")) VISIBLE else GONE
        })

        EventHub.instance.addListener(EventListener("infoviewlowtemp", EventType.LowTemp){
            imgCold.visibility = if (it.getBoolean("lowtemp")) View.VISIBLE else GONE
        })
    }

    fun detach()  {
        EventHub.instance.removeListener("infoviewtemperature")
        EventHub.instance.removeListener("infoviewhightemp")
        EventHub.instance.removeListener("infoviewlowtemp")
    }
}