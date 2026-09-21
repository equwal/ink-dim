// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 equwal
package dev.equwal.inkdim

/**
 * The decision and the shell commands. No Android types, so it is all testable.
 *
 * Measured on the Viwoods reader: the frontlight is an ordinary backlight LED.
 * Its driver accepts 1 as the lowest lit value, but the framework will not go
 * below 5. Ask for 4 through any official route and it snaps to zero. A write
 * straight to the LED node gets under that floor, and the value then holds,
 * even across sleep, until the system brightness is next set.
 *
 * The node belongs to `system`, so a plain shell user cannot write it. These
 * readers ship a userdebug build, where the shell user may run `su 0`. So each
 * write tries the plain write first and falls back to `su 0`.
 */
object Dim {

    /** What the caller asked for. */
    enum class Action { TOGGLE, ON, OFF }

    /** The light nodes to try, in order. The first one that exists wins. */
    val nodes = listOf(
        "/sys/class/leds/lcd-backlight/brightness",
        "/sys/class/backlight/panel0-backlight/brightness"
    )

    /** The value to hold the light at. The lowest value the driver still lights. */
    const val ON_VALUE = 1

    /**
     * The state the light is in now.
     *
     * The node wins, because the user can change the brightness in the system
     * at any time. [remembered] is the fallback when the node could not be read.
     */
    fun isOn(nodeValue: Int?, remembered: Boolean): Boolean =
        if (nodeValue == null) remembered else nodeValue <= ON_VALUE

    /** The state to go to. */
    fun next(nodeValue: Int?, remembered: Boolean, action: Action): Boolean = when (action) {
        Action.ON -> true
        Action.OFF -> false
        Action.TOGGLE -> !isOn(nodeValue, remembered)
    }

    // ---- shell commands ----------------------------------------------------

    /**
     * Writes [value] to the first node that exists. A plain write first, then
     * through `su 0`, then a read back to prove the value took.
     *
     * Exit 0 the light holds [value], 1 the write was refused, 2 there is no
     * node on this device.
     */
    fun writeCommand(nodes: List<String>, value: Int): String {
        val list = nodes.joinToString(" ") { "'$it'" }
        return "for n in $list; do [ -e \$n ] || continue; " +
            "echo $value > \$n 2>/dev/null || su 0 sh -c \"echo $value > \$n\"; " +
            "[ \"\$(cat \$n)\" = \"$value\" ] && exit 0; exit 1; done; exit 2"
    }

    /**
     * Prints the value of the first node that exists. Exit 1 means this device
     * has no light node that this app knows.
     */
    fun readCommand(nodes: List<String>): String {
        val list = nodes.joinToString(" ") { "'$it'" }
        return "for n in $list; do [ -e \$n ] || continue; cat \$n; exit 0; done; exit 1"
    }

    /**
     * Gives the light back to the system.
     *
     * The system brightness goes up by one and straight back. That makes the
     * framework write the node again, with the value the user chose. So "off"
     * always lands on the brightness the system holds, whatever it is.
     */
    fun restoreCommand(): String =
        "v=\$(settings get system screen_brightness); " +
            "settings put system screen_brightness \$((v+1)); " +
            "settings put system screen_brightness \$v"

    /** The value the node holds, or null. Never throws. */
    fun parseRead(output: String): Int? =
        output.lineSequence().map { it.trim() }.firstNotNullOfOrNull { it.toIntOrNull() }
}
