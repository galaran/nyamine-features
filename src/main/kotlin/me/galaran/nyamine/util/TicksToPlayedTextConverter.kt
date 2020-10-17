package me.galaran.nyamine.util

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import java.time.Duration

object TicksToPlayedTextConverter {

    fun convert(ticks: Int): BaseComponent {
        val duration = Duration.ofSeconds((ticks / 20).toLong())
        val days = duration.toDays().toInt()
        val hours = duration.toHoursPart()
        val minutes = duration.toMinutesPart()

        val result = "Наиграно ".toComponent(ChatColor.GRAY)
        if (days > 0) {
            result.addExtra("$days ${TimeUnitRuForms.DAY.forValue(days)} ")
        }
        if (days > 0 || hours > 0) {
            result.addExtra("$hours ${TimeUnitRuForms.HOUR.forValue(hours)} ")
        }
        result.addExtra("$minutes ${TimeUnitRuForms.MINUTE.forValue(minutes)}")

        return result
    }

    private enum class TimeUnitRuForms(
            private val firstForm: String,
            private val secondForm: String,
            private val thirdForm: String
    ) {
        DAY("дней", "дня", "день"),
        HOUR("часов", "часа", "час"),
        MINUTE("минут", "минуты", "минута");

        fun forValue(value: Int): String {
            return when {
                value in 5..19 -> firstForm
                value % 10 in 2..4 -> secondForm
                value % 10 == 1 -> thirdForm
                else -> firstForm
            }
        }
    }
}
