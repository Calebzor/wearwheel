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

class InfoElectric : LinearLayout {
    private lateinit var txtAmps : TextView
    private lateinit var txtVolts : TextView
    private lateinit var imgRegen : ImageView

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
        LayoutInflater.from(context).inflate(R.layout.info_electric, this, true)

        txtAmps = findViewById(R.id.txtAmps)
        txtVolts = findViewById(R.id.txtVolts)
        imgRegen = findViewById(R.id.imgRegen)


        setOnSystemUiVisibilityChangeListener {
            if (it == View.VISIBLE) attach() else detach()
        }
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
        val regenDetector = ValueAverager(1500L, -0.1f){
            EventHub.instance.post(EventType.Regen, Bundle().apply { putBoolean("regen", it) })
        }

        EventHub.instance.addListener(EventListener("infoviewamps", EventType.Amps) {
            val amps = it.getFloat("amps")

            txtAmps.text = "%.1f".format(amps)

            regenDetector.update(amps)
        })

        EventHub.instance.addListener(EventListener("infoviewvolts", EventType.Volts) {
            txtVolts.text = it.getFloat("volts").roundToInt().toString()
        })

        EventHub.instance.addListener(EventListener("infoviewregen", EventType.Regen) {
            imgRegen.visibility = if (it.getBoolean("regen")) View.VISIBLE else View.GONE
        })
    }

    fun detach() {
        EventHub.instance.apply {
            removeListener("infoviewamps")
            removeListener("infoviewvolts")
            removeListener("infoviewregen")
        }

        super.onDetachedFromWindow()
    }
}