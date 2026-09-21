// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 equwal
package dev.equwal.inkdim

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.LinearLayout
import dev.equwal.inkdim.Ui.button
import dev.equwal.inkdim.Ui.header
import dev.equwal.inkdim.Ui.note
import dev.equwal.inkdim.Ui.primaryButton
import dev.equwal.inkdim.Ui.row

/**
 * What the app needs, and what the app is.
 *
 * The launcher icon only toggles, so this screen opens from the icon shortcut,
 * or by itself when the app cannot do the toggle.
 */
class SettingsActivity : Activity() {

    /** Null until the light node is looked for. */
    private var light: Boolean? = null

    private val redraw: () -> Unit = { if (!isFinishing) build() }

    override fun onResume() {
        super.onResume()
        Shell.onChange(redraw)
        Shell.connect(this)
        build()
        if (Shell.ready) Toggle.hasNode { found -> light = found; redraw() }
    }

    override fun onPause() {
        super.onPause()
        Shell.removeOnChange(redraw)
    }

    private fun build() {
        val col = Ui.page(this, "Ink Dim")

        col.header("Extra dim")
        col.note("Sets the frontlight below the lowest level of the system.")
        col.note("Tap the icon to turn it on. Tap it again for the system level.")
        col.row("Light", null, enabled = false, state = lightState())
        if (light == false) col.note("This device has no light that the app knows.")
        col.primaryButton("Try it") { Toggle.run(this, Dim.Action.TOGGLE) }

        col.header("Shizuku")
        col.note("The light belongs to the system. Shizuku gives the app a shell. The shell then writes the light.")
        col.row("State", null, enabled = false, state = Shell.describe(this))
        if (Shell.state(this) != Shell.State.READY) steps(col)

        col.header("About")
        col.row("Version", null, enabled = false, state = BuildConfig.VERSION_NAME)
        col.row("Licence", null, enabled = false, state = "GPL-3.0-or-later")
        col.row("Source code", REPO.removePrefix("https://")) { start(Intent(Intent.ACTION_VIEW, Uri.parse(REPO))) }
        col.row("Buy me a coffee", TIP.removePrefix("https://")) { start(Intent(Intent.ACTION_VIEW, Uri.parse(TIP))) }
    }

    private fun lightState(): String = when {
        !Shell.ready -> "Needs Shizuku"
        light == null -> "Looking"
        light == true -> "Found"
        else -> "None"
    }

    /** The four steps to shell access, from the device itself. */
    private fun steps(col: LinearLayout) {
        val state = Shell.state(this)
        col.note("1. Install Shizuku. It is free.")
        if (state == Shell.State.NOT_INSTALLED) {
            col.button("Get Shizuku") {
                if (!start(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Shell.SHIZUKU_PACKAGE)))) {
                    start(Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/download/")))
                }
            }
        }

        col.note("2. Turn on wireless debugging in Developer options.")
        col.button("Open Developer options") {
            if (!start(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))) {
                start(Intent(Settings.ACTION_DEVICE_INFO_SETTINGS))
            }
        }

        col.note("3. In Shizuku, pair with the code, then press Start.")
        if (state != Shell.State.NOT_INSTALLED) {
            col.button("Open Shizuku") {
                packageManager.getLaunchIntentForPackage(Shell.SHIZUKU_PACKAGE)?.let { start(it) }
            }
        }

        col.note("4. Let Ink Dim use Shizuku.")
        if (state == Shell.State.NO_PERMISSION) {
            col.button("Ask for permission") { Shell.requestPermission() }
        }
    }

    private fun start(i: Intent): Boolean =
        runCatching { startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); true }.getOrDefault(false)

    private companion object {
        const val REPO = "https://github.com/equwal/ink-dim"
        const val TIP = "https://ko-fi.com/truex"
    }
}
