package me.galaran.nyamine.util.text

import java.time.Duration

object TicksToPlayedTextConverter {

    fun convert(ticks: Int): String {
        val duration = Duration.ofSeconds((ticks / 20).toLong())
        val days = duration.toDays().toInt()
        val hours = duration.toHoursPart()
        val minutes = duration.toMinutesPart()

        var result = ""
        if (days > 0) {
            result += "$days ${PluralRuForms.DAY.forValue(days)} "
        }
        if (days > 0 || hours > 0) {
            result += "$hours ${PluralRuForms.HOUR.forValue(hours)} "
        }
        result += "$minutes ${PluralRuForms.MINUTE.forValue(minutes)}"

        return result
    }
}
