package com.littlesunsoftware.wearwheel

import java.util.*

class Units {
    enum class UnitSystem {
        Metric,
        Imperial_US,
        Imperial_UK
    }

    constructor() {}

    fun getTemperature(celsius: Float) : Float {
        return when (getUnitSystem()) {
            UnitSystem.Imperial_US -> (celsius * (9f/5f)) + 32f
            else -> celsius
        }
    }

    fun getRate(kph: Float) : Float {
        return when (getUnitSystem()) {
            UnitSystem.Metric -> kph
            else -> kph / 1.609f
        }
    }

    fun getRateLabel() : String{
        return when (getUnitSystem()) {
            UnitSystem.Metric -> "kph"
            else -> "mph"
        }
    }

    fun getDistance(kilometers: Float) : Float {
        return when (getUnitSystem()) {
            UnitSystem.Metric -> kilometers
            else -> kilometers / 1.609f
        }
    }

    fun getDistanceLabel() : String{
        return when (getUnitSystem()) {
            UnitSystem.Metric -> "kilometers"
            else -> "miles"
        }
    }

    fun getDistanceLabelShort() : String{
        return when (getUnitSystem()) {
            UnitSystem.Metric -> "Km"
            else -> "Mi"
        }
    }

    private fun getUnitSystem() : UnitSystem {
        return when (Locale.getDefault()) {
            Locale.US -> UnitSystem.Imperial_US
            Locale.UK -> UnitSystem.Imperial_UK
            else -> UnitSystem.Metric
        }
    }
}