// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 equwal
package dev.equwal.inkdim

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/** A tile in the quick settings panel. It does the same as the icon. */
class DimTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        paint(Toggle.remembered(this))
        Shell.connect(this)
        if (Shell.ready) Toggle.live(this) { paint(it) }
    }

    override fun onClick() {
        super.onClick()
        Shell.connect(this)
        Toggle.run(this, Dim.Action.TOGGLE) { state -> if (state != null) paint(state) }
    }

    private fun paint(on: Boolean) {
        qsTile?.apply {
            this.state = if (on) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTile()
        }
    }
}
