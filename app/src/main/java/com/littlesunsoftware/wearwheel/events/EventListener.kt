package com.littlesunsoftware.wearwheel.events

import android.os.Bundle

class EventListener {
    var eventType : EventType
    var handler : (Bundle) -> Unit
    var name : String

    constructor(name: String, eventType: EventType, handler: (Bundle) -> Unit) {
        this.eventType = eventType
        this.handler = handler
        this.name = name
    }
}