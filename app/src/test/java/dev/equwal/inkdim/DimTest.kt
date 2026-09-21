// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 equwal
package dev.equwal.inkdim

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

/** Tests for the pure functions of [Dim]. */
class DimTest {

    // ---- Dim.next ----------------------------------------------------------

    @Test fun `the node value wins over what the app remembers`() {
        // The node says the light is already down, so a toggle goes back up.
        assertFalse(Dim.next(1, false, Dim.Action.TOGGLE))
        // The node says the light is up, so a toggle goes down.
        assertTrue(Dim.next(60, true, Dim.Action.TOGGLE))
    }

    @Test fun `a node value at or below the on value counts as on`() {
        assertFalse(Dim.next(0, false, Dim.Action.TOGGLE))
        assertFalse(Dim.next(Dim.ON_VALUE, false, Dim.Action.TOGGLE))
        assertTrue(Dim.next(Dim.ON_VALUE + 1, false, Dim.Action.TOGGLE))
    }

    @Test fun `no node value falls back to what the app remembers`() {
        assertFalse(Dim.next(null, true, Dim.Action.TOGGLE))
        assertTrue(Dim.next(null, false, Dim.Action.TOGGLE))
    }

    @Test fun `on and off give the same answer whatever the state is`() {
        for (node in listOf(null, 0, 1, 5, 128, 255)) {
            for (remembered in listOf(true, false)) {
                assertTrue(Dim.next(node, remembered, Dim.Action.ON))
                assertFalse(Dim.next(node, remembered, Dim.Action.OFF))
            }
        }
    }

    // ---- Dim.parseRead -----------------------------------------------------

    @Test fun `the reader takes the value the node prints`() {
        assertEquals(1, Dim.parseRead("1\n"))
        assertEquals(60, Dim.parseRead("  60  \n"))
        assertEquals(255, Dim.parseRead("255"))
    }

    @Test fun `the reader gives nothing for output that holds no value`() {
        assertNull(Dim.parseRead(""))
        assertNull(Dim.parseRead("\n \n"))
        assertNull(Dim.parseRead("/system/bin/sh: cat: No such file or directory"))
        assertNull(Dim.parseRead("null"))
    }

    /**
     * Junk input must never throw. The generator makes output that is close to
     * the real thing, because a value that looks right but does not fit an Int
     * is what breaks a reader.
     */
    @Test fun `the reader never throws on junk`() {
        val r = Random(13)
        val numbers = listOf(
            "0", "1", "5", "255", "-3", "+7", "007",
            "99999999999999999999", "2147483648", "1.5", "1 2"
        )
        val noise = listOf(
            "", " ", "\t", "\n", "\r\n", "null", "cat:", "Permission denied",
            "/sys/class/leds/lcd-backlight/brightness", "su:", "not found", "💩"
        )

        fun pick(xs: List<String>) = xs[r.nextInt(xs.size)]

        repeat(2000) {
            val text = (0 until r.nextInt(5)).joinToString("\n") {
                pick(noise + numbers) + pick(noise) + pick(numbers + noise)
            }
            // Nothing is claimed about the result. It must only come back.
            Dim.parseRead(text)
        }
    }

    // ---- the shell commands ------------------------------------------------

    @Test fun `the write falls back to su and quotes every node`() {
        val command = Dim.writeCommand(Dim.nodes, Dim.ON_VALUE)
        assertTrue(command.contains("su 0"))
        for (node in Dim.nodes) assertTrue(command.contains("'$node'"))
        assertTrue(Dim.nodes.isNotEmpty())
    }

    @Test fun `the read quotes every node`() {
        val command = Dim.readCommand(Dim.nodes)
        for (node in Dim.nodes) assertTrue(command.contains("'$node'"))
    }

    @Test fun `off puts back the brightness the system holds`() {
        assertTrue(Dim.restoreCommand().contains("settings put system screen_brightness"))
    }
}
