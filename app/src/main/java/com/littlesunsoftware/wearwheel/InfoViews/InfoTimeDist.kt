package com.littlesunsoftware.wearwheel.InfoViews

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
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
import java.util.*
import kotlin.math.roundToInt
import javax.xml.datatype.DatatypeConstants.HOURS
import com.littlesunsoftware.wearwheel.Units
import java.util.concurrent.TimeUnit


class InfoTimeDist : LinearLayout {
    private lateinit var txtTime : TextView
    private lateinit var txtDist: TextView
    private lateinit var txtDistLabel: TextView

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
        LayoutInflater.from(context).inflate(R.layout.info_timedist, this, true)

        txtTime = findViewById(R.id.txtTime)
        txtDist = findViewById(R.id.txtDistance)
        txtDistLabel = findViewById(R.id.txtDistanceLabel)

        setOnSystemUiVisibilityChangeListener {
            if (it == View.VISIBLE) attach() else detach()
        }

        txtDistLabel.text = "Dist (${Units().getDistanceLabelShort()})"
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
        EventHub.instance.addListener(EventListener("infoviewtime", EventType.Time) {
            val millis = it.getLong("time")
            val hms = String.format("%01d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)))
            txtTime.text = hms
        })

        EventHub.instance.addListener(EventListener("infoviewdistance", EventType.Distance) {
            txtDist.text = "%.1f".format(it.getFloat("distance"))
        })
    }

    fun detach() {
        EventHub.instance.apply {
            removeListener("infoviewtime")
            removeListener("infoviewdistance")
        }

        super.onDetachedFromWindow()
    }
}