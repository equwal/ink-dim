// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 equwal
package dev.equwal.inkdim

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper

/**
 * The launcher icon. It has no screen.
 *
 * A tap toggles the light and the activity goes away. There is no toast: this
 * device drops them, and the light itself is the answer.
 *
 * Without shell access, or without a light node, it opens the settings screen
 * instead, which says what to do.
 */
class ToggleActivity : Activity() {

    private val main = Handler(Looper.getMainLooper())
    private var acted = false

    /** Shizuku hands over its binder a moment after the app starts. */
    private val onShell: () -> Unit = { if (Shell.ready) act() }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        Shell.onChange(onShell)
        Shell.connect(this)
        if (Shell.ready) act() else main.postDelayed({ if (!acted) openSettings() }, WAIT_MS)
    }

    override fun onDestroy() {
        super.onDestroy()
        Shell.removeOnChange(onShell)
        main.removeCallbacksAndMessages(null)
    }

    private fun act() {
        if (acted) return
        acted = true
        val action = when (intent?.action) {
            ACTION_ON -> Dim.Action.ON
            ACTION_OFF -> Dim.Action.OFF
            else -> Dim.Action.TOGGLE
        }
        Toggle.run(this, action) { result ->
            if (result == null) openSettings() else close()
        }
    }

    private fun openSettings() {
        acted = true
        startActivity(Intent(this, SettingsActivity::class.java))
        close()
    }

    @Suppress("DEPRECATION")
    private fun close() {
        finish()
        overridePendingTransition(0, 0)
    }

    private companion object {
        const val ACTION_ON = "dev.equwal.inkdim.ON"
        const val ACTION_OFF = "dev.equwal.inkdim.OFF"

        /** How long to wait for Shizuku to hand over its binder. */
        const val WAIT_MS = 1500L
    }
}
