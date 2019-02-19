package com.littlesunsoftware.wearwheel

enum class LogDetail(value: Int) {
    normal(0),
    verbose(1)
}

class Logger {
    companion object {
        var currentPriority = LogDetail.normal

        fun Log(message: String, priority : LogDetail = LogDetail.normal) {
            if (priority.ordinal >= currentPriority.ordinal)
                android.util.Log.d("wheelwear", message)
        }
    }
}