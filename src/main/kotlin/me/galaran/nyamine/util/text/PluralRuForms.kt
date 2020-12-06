package me.galaran.nyamine.util.text

enum class PluralRuForms(
        private val firstForm: String,
        private val secondForm: String,
        private val thirdForm: String
) {
    DAY("дней", "дня", "день"),
    HOUR("часов", "часа", "час"),
    MINUTE("минут", "минуты", "минута"),
    VILLAGER("жителей", "жителя", "житель");

    fun forValue(value: Int): String {
        return when {
            value in 5..19 -> firstForm
            value % 10 in 2..4 -> secondForm
            value % 10 == 1 -> thirdForm
            else -> firstForm
        }
    }
}
