package com.example.autopark.util

import com.example.autopark.data.model.ParkingRate
import java.util.Calendar

object ParkingChargeCalculator {
    
    fun calculateCharge(
        startTime: Long,
        endTime: Long,
        rate: ParkingRate?,
        isVIP: Boolean = false
    ): Double {
        if (rate == null) return 0.0

        val durationMs = endTime - startTime
        val durationHours = durationMs.toDouble() / (60 * 60 * 1000)
        val durationDays = durationHours / 24

        var charge = 0.0

        // Check if it's overnight parking
        val isOvernight = isOvernightParking(startTime, endTime)

        charge = when {
            isOvernight && rate.overnightPrice > 0 -> {
                rate.overnightPrice
            }
            durationDays >= 1 && rate.pricePerDay > 0 -> {
                val fullDays = (durationDays).toInt()
                val remainingHours = durationHours - (fullDays * 24)
                (fullDays * rate.pricePerDay) + (remainingHours * (rate.pricePerDay / 24))
            }
            else -> {
                durationHours * rate.pricePerHour
            }
        }

        // Apply VIP multiplier if applicable
        if (isVIP && rate.vipMultiplier > 1.0) {
            charge *= rate.vipMultiplier
        }

        // Apply minimum charge
        if (charge < rate.minChargeAmount) {
            charge = rate.minChargeAmount
        }

        // Apply maximum charge per day
        if (durationDays > 1 && charge > rate.maxChargePerDay) {
            charge = rate.maxChargePerDay
        }

        return CurrencyFormatter.roundToTwoDecimals(charge)
    }

    private fun isOvernightParking(startTime: Long, endTime: Long): Boolean {
        val startCal = Calendar.getInstance()
        startCal.timeInMillis = startTime
        val endCal = Calendar.getInstance()
        endCal.timeInMillis = endTime

        val startHour = startCal.get(Calendar.HOUR_OF_DAY)
        val endHour = endCal.get(Calendar.HOUR_OF_DAY)

        // Check if parking spans overnight (after 8 PM or before 8 AM)
        return (startHour >= Constants.OVERNIGHT_START_HOUR || startHour < Constants.OVERNIGHT_END_HOUR) &&
               (endHour >= Constants.OVERNIGHT_START_HOUR || endHour < Constants.OVERNIGHT_END_HOUR)
    }

    fun calculateOverdueCharge(originalAmount: Double, daysOverdue: Int, lateFeePercentage: Double = 10.0): Double {
        val lateFee = (originalAmount * lateFeePercentage) / 100.0
        return CurrencyFormatter.roundToTwoDecimals(lateFee * daysOverdue)
    }

    fun estimateChargeForDuration(
        durationHours: Long,
        rate: ParkingRate?,
        isVIP: Boolean = false
    ): Double {
        if (rate == null) return 0.0

        val durationInHours = durationHours.toDouble()
        val durationInDays = durationInHours / 24

        var charge = 0.0

        charge = when {
            durationInDays >= 1 && rate.pricePerDay > 0 -> {
                val fullDays = durationInDays.toInt()
                val remainingHours = durationInHours - (fullDays * 24)
                (fullDays * rate.pricePerDay) + (remainingHours * (rate.pricePerDay / 24))
            }
            rate.pricePerHour > 0 -> {
                durationInHours * rate.pricePerHour
            }
            else -> rate.minChargeAmount
        }

        // Apply VIP multiplier if applicable
        if (isVIP && rate.vipMultiplier > 1.0) {
            charge *= rate.vipMultiplier
        }

        // Apply minimum charge
        if (charge < rate.minChargeAmount) {
            charge = rate.minChargeAmount
        }

        return CurrencyFormatter.roundToTwoDecimals(charge)
    }
}
