// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 equwal
package dev.equwal.inkdim

import android.content.Context

/**
 * The one action of this app, for the icon, the tile and the intent actions.
 *
 * It reads the light node first, so the state is what the light really does,
 * not only what this app last did. Then it writes, and remembers the result.
 */
object Toggle {

    private const val PREFS = "inkdim"
    private const val K_ON = "on"

    fun remembered(c: Context): Boolean =
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(K_ON, false)

    private fun remember(c: Context, on: Boolean) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(K_ON, on).apply()
    }

    /**
     * Does [action]. [done] gets the new state, or null when the app could not
     * do it: no shell access, or no light node on this device.
     */
    fun run(c: Context, action: Dim.Action, done: (Boolean?) -> Unit = {}) {
        Shell.run(Dim.readCommand(Dim.nodes)) { read ->
            val value = Dim.parseRead(read.output)
            if (!read.ok && value == null) return@run done(null)
            val on = Dim.next(value, remembered(c), action)
            remember(c, on)
            val command = if (on) Dim.writeCommand(Dim.nodes, Dim.ON_VALUE) else Dim.restoreCommand()
            Shell.run(command) { done(if (it.ok) on else null) }
        }
    }

    /** The state the light is in now. It also corrects what the app remembers. */
    fun live(c: Context, done: (Boolean) -> Unit) {
        Shell.run(Dim.readCommand(Dim.nodes)) { read ->
            val on = Dim.isOn(Dim.parseRead(read.output), remembered(c))
            remember(c, on)
            done(on)
        }
    }

    /** True when this device has a light node that the app knows. */
    fun hasNode(done: (Boolean) -> Unit) {
        Shell.run(Dim.readCommand(Dim.nodes)) { done(it.ok && Dim.parseRead(it.output) != null) }
    }
}
