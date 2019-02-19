package com.littlesunsoftware.wearwheel.events

class ValueAverager(timeSpanMS: Long, threshold: Float, callBack: (aboveThreshold: Boolean) -> Unit) {
    private val timeSpanMS = timeSpanMS
    private class TimeValue(val time : Long, val value : Float)
    private var threshold = threshold
    private var callBack = callBack
    private var timeValues = ArrayList<TimeValue>()
    private var oldAverage : Float? = null

    fun update(value : Float) {
        val currTime = System.currentTimeMillis()

        timeValues.add(TimeValue(currTime, value))

        // Clear out old values from the list (todo make an n-sized queue or something more efficient)
        val cutoff = currTime - timeSpanMS
        timeValues.removeAll { it.time <= cutoff }

        // Get the new average value
        val average = getAverage()

        // Did the new average value cross the threshold compared to the old average value
        if (oldAverage != null) {
            val oldAverageThresholdSide = (oldAverage!! < threshold)
            val newAverageThresholdSide = (average < threshold)

            if (oldAverageThresholdSide != newAverageThresholdSide) {
                callBack(newAverageThresholdSide)
            }
        }
        oldAverage = average
    }

    fun getAverage() : Float {
        if (timeValues.count() == 1) return timeValues.first().value

        val sum = timeValues
                .map { it.value }
                .reduce { a, b -> a + b}

        return sum / timeValues.count().toFloat()
    }
}