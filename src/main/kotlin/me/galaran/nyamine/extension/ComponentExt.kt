package me.galaran.nyamine.extension

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.flattener.ComponentFlattener
import net.kyori.adventure.text.format.NamedTextColor

infix fun Any.colored(color: NamedTextColor) = Component.text(this.toString(), color)

fun Component.appendNonNull(other: Component?): Component {
    return if (other != null) this.append(other) else this
}

operator fun Component.plus(other: Component): Component = appendNonNull(other)

fun Component.toBasicString(): String {
    val result = StringBuilder()
    ComponentFlattener.basic().flatten(this, result::append)
    return result.toString()
}
