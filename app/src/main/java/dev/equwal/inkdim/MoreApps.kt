// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 equwal
package dev.equwal.inkdim

/**
 * The other sites and apps of the same author, for the settings screen. They are
 * in the order of the catalog: the sites, then the Android apps, then the list of
 * all projects. Ink Dim is not in it.
 */
object MoreApps {

    /** One link: a name, one line about it, and the page that a tap opens. */
    class App(val name: String, val line: String, val url: String)

    val ALL = listOf(
        App("SubRead", "Read along with an audiobook.", "https://subread.space/"),
        App("Book Simulator", "A reading room for Aozora Bunko and Project Gutenberg books.", "https://booksimulator.com/"),
        App("honjimaku.com", "Subtitles for Japanese audiobooks.", "https://honjimaku.com/"),
        App("sbm Sync", "Your bookmarks, the same on every device.", "https://sbmsync.com/"),
        App(
            "SubRead for Android", "Times an audiobook against its ebook on the device.",
            "https://github.com/equwal/subread-android/releases/latest"
        ),
        App(
            "SubRead Overlay", "Subtitle lines over any Android media player.",
            "https://github.com/equwal/subread-overlay/releases/latest"
        ),
        App(
            "SubRead Dictionary", "Pop-up dictionary that reads Yomitan dictionaries.",
            "https://github.com/equwal/subread-dictionary/releases/latest"
        ),
        App("SubRead Anki", "One tap makes an Anki card from any app.", "https://github.com/equwal/subread-anki"),
        App(
            "Subrep", "Live captions of the sound of your phone.",
            "https://github.com/equwal/subrep-android/releases/latest"
        ),
        App(
            "sbm for Android", "Fuzzy search for your bookmarks.",
            "https://github.com/equwal/sbm-android/releases/latest"
        ),
        App(
            "Rebind", "Remap the hardware buttons of e-ink readers and Android.",
            "https://github.com/equwal/rebind/releases"
        ),
        App(
            "Ink Recents", "A recent-apps switcher for e-ink.",
            "https://github.com/equwal/ink-recents/releases/latest"
        ),
        App(
            "Ink Update", "Tells you when Rebind and its extensions update.",
            "https://github.com/equwal/ink-update/releases/latest"
        ),
        App("All projects", "Everything, with source code.", "https://recentlywritten.com/projects.html")
    )
}
