package com.littlesunsoftware.wearwheel.events

import android.os.Bundle

class EventHub {
    private var listeners = ArrayList<EventListener>()


    companion object {
        @JvmField
        val instance = EventHub()
    }

    fun post(eventType: EventType, bundle: Bundle) {
        listeners.filter { it.eventType == eventType }
            .forEach {
                it.handler(bundle)
            }
    }

    fun addListener(listener: EventListener) {
        removeListener(listener.name)
        listeners.add(listener)
    }

    fun removeListener(name: String) {
        listeners.removeAll { it.name == name }
    }
}