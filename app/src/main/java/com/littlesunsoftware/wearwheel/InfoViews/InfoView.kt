package com.littlesunsoftware.wearwheel.InfoViews

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.littlesunsoftware.wearwheel.R
import com.littlesunsoftware.wearwheel.events.EventHub
import com.littlesunsoftware.wearwheel.events.EventListener
import com.littlesunsoftware.wearwheel.events.EventType

class InfoView : ConstraintLayout {
    var views = ArrayList<View>()

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
        LayoutInflater.from(context).inflate(R.layout.info_view, this, true)

        views.add(findViewById(R.id.infoTimeDist))
        views.add(findViewById(R.id.infoElectric))
        views.add(findViewById(R.id.infoTemperature))

        showView(R.id.infoTimeDist)

        EventHub.instance.addListener(EventListener("infoviewtoggle", EventType.UserToggleInfoView) {
            toggleViews()
        })

        setOnClickListener { EventHub.instance.post(EventType.UserToggleInfoView, Bundle()) }
    }

    private fun showView(viewId: Int) {
        views.find { it.id == viewId }?.apply {
            visibility = VISIBLE
        }
        views.filter {it.id != viewId }.forEach { it.visibility = GONE }
    }

    private fun toggleViews() {
        val newIndex = (views.indexOfFirst { it.visibility == View.VISIBLE } + 1) % views.count()
        showView(views[newIndex].id)
    }
}
