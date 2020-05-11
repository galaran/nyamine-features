package me.galaran.nyamine

import org.bukkit.ChatColor

enum class ReturnChorusGrade(
        val displayName: String,
        val lore: String?,
        val nameColor: ChatColor,
        val enchantLevel: Int,
        val returnDelayTicks: Int
) {
    COMMON("Хорус возвращения", null, ChatColor.YELLOW, 1, 240),
    FAST("Хорус быстрого возвращения", null, ChatColor.AQUA, 5, 70),
    INSTANT("Хорус мгновенного возвращения", "хоме! Хоме! ХОМЕ!", ChatColor.LIGHT_PURPLE, 10, 2)
}
