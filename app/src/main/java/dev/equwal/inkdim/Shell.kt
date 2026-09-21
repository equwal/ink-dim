// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 equwal
package dev.equwal.inkdim

import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import rikka.shizuku.Shizuku
import java.util.concurrent.Executors

/**
 * Shell access, by way of Shizuku.
 *
 * Shizuku is a separate, free app. The user starts it from the device itself
 * through Android's wireless debugging. There is no computer and no root. Once
 * it runs and has said yes to this app, this object can start a process under
 * the shell uid. That is what writes the light node.
 *
 * It uses Shizuku's plain remote-process call and nothing more. Shizuku's bound
 * "user service" is the tidier design, but its starter dies inside
 * LoadedApk.makeApplication on Android 16 before our code is loaded.
 */
object Shell {

    private const val TAG = "InkDim"

    const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
    private const val REQUEST_CODE = 7301

    enum class State { NOT_INSTALLED, NOT_RUNNING, NO_PERMISSION, READY }

    private val main = Handler(Looper.getMainLooper())
    private val worker = Executors.newSingleThreadExecutor()
    private val listeners = ArrayList<() -> Unit>()
    private var hooked = false

    val ready: Boolean get() = alive() && permitted()

    fun state(c: Context): State = when {
        ready -> State.READY
        !installed(c) -> State.NOT_INSTALLED
        !alive() -> State.NOT_RUNNING
        else -> State.NO_PERMISSION
    }

    fun describe(c: Context): String = when (state(c)) {
        State.READY -> "On"
        State.NO_PERMISSION -> "Shizuku runs, permission needed"
        State.NOT_RUNNING -> "Shizuku is installed, not started"
        State.NOT_INSTALLED -> "Off, needs the free Shizuku app"
    }

    /** Called when the state can have changed. Always on the main thread. */
    fun onChange(l: () -> Unit) { if (l !in listeners) listeners.add(l) }
    fun removeOnChange(l: () -> Unit) { listeners.remove(l) }
    private fun changed() = main.post { ArrayList(listeners).forEach { it() } }

    private fun installed(c: Context): Boolean =
        runCatching { c.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0); true }.getOrDefault(false)

    private fun alive(): Boolean = runCatching { Shizuku.pingBinder() }.getOrDefault(false)

    private fun permitted(): Boolean = runCatching {
        !Shizuku.isPreV11() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }.getOrDefault(false)

    /** Safe to call often. It registers for Shizuku's comings and goings once. */
    fun connect(c: Context) {
        if (!hooked) {
            hooked = true
            runCatching {
                Shizuku.addBinderReceivedListenerSticky { changed() }
                Shizuku.addBinderDeadListener { changed() }
                Shizuku.addRequestPermissionResultListener { _, _ -> changed() }
            }
        }
        changed()
    }

    fun requestPermission() {
        runCatching { if (alive() && !permitted()) Shizuku.requestPermission(REQUEST_CODE) }
    }

    // ---- processes ---------------------------------------------------------

    /**
     * Shizuku.newProcess is private in the v13 API, but it is the supported wire
     * call underneath, and the one that every shell-style client uses.
     */
    private val newProcess by lazy {
        Shizuku::class.java.getDeclaredMethod(
            "newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java
        ).apply { isAccessible = true }
    }

    private fun start(vararg argv: String): Process? = runCatching {
        newProcess.invoke(null, argv, null, null) as Process
    }.onFailure { Log.w(TAG, "shell process failed: " + argv.joinToString(" "), it) }.getOrNull()

    data class Result(val exit: Int, val output: String) {
        val ok: Boolean get() = exit == 0
    }

    private fun execNow(command: String): Result {
        val p = start("sh", "-c", "$command 2>&1") ?: return Result(-1, "no shell access")
        return runCatching {
            val out = p.inputStream.bufferedReader().readText()
            Result(p.waitFor(), out)
        }.getOrElse { Result(-1, it.toString()) }
    }

    /** Runs off the main thread. [done] comes back on the main thread. */
    fun run(command: String, done: ((Result) -> Unit)? = null) {
        worker.execute {
            val r = if (ready) execNow(command) else Result(-1, "no shell access")
            if (!r.ok) Log.i(TAG, "shell: " + command + " -> " + r.exit + " " + r.output.take(200))
            done?.let { main.post { it(r) } }
        }
    }
}
