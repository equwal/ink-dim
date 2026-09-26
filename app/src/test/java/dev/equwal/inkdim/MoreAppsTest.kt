// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 equwal
package dev.equwal.inkdim

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests for the list of the More apps section. */
class MoreAppsTest {

    @Test fun `the list leaves out Ink Dim`() {
        assertFalse(MoreApps.ALL.any { it.name == "Ink Dim" || "equwal/ink-dim" in it.url })
    }

    @Test fun `each link opens an https page`() {
        for (app in MoreApps.ALL) assertTrue(app.url, app.url.startsWith("https://"))
    }

    @Test fun `the list keeps the order of the catalog`() {
        assertEquals(
            listOf("SubRead", "Book Simulator", "honjimaku.com", "sbm Sync"),
            MoreApps.ALL.take(4).map { it.name }
        )
        assertEquals("All projects", MoreApps.ALL.last().name)
        // The catalog has 15 entries. Ink Dim is the one that is not in the list.
        assertEquals(14, MoreApps.ALL.size)
    }
}
