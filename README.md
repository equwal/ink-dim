# Ink Dim

Sets the frontlight below the lowest level of the system.

Tap the icon. The light goes to the lowest level the hardware still lights.
Tap it again. The light goes back to the level the system holds.

The app has no screen. The icon is the whole app.

Made for the Viwoods AiPaper readers.

## Screenshots

To be added.

## Why the app needs Shizuku

The system will not set the frontlight below its own floor. Ask for less
through any official route and the light snaps to zero. So the app writes the
light of the panel straight, at
`/sys/class/leds/lcd-backlight/brightness`.

That file belongs to the user `system`. An app cannot write it, and a plain
shell user cannot write it either. The AiPaper Reader that this was made on has
a userdebug build, where the shell user may run `su 0`. On a reader with
another build, the app says that it could not set the light. So the app asks Shizuku for a shell, and the
shell writes the file through `su 0`.

Shizuku is a separate free app. You start it from the device through wireless
debugging. There is no computer and no root.

Long press the icon and open **Settings** for the steps, with a button for each
one.

## Install

Download the APK from the Releases page and install it.

Then long press the icon, open **Settings**, and follow the steps.

## Use

- Tap the icon. The light goes down.
- Tap the icon again. The light goes back to the system level.
- Long press the icon for **Settings**.

The value holds across sleep. It holds until the system brightness is next set.

## Quick settings tile

The tile is called **Extra dim**. Add it to the quick settings panel, then tap
it. The tile does the same as the icon, and it shows the state.

## Start it from somewhere else

Three intent actions:

    dev.equwal.inkdim.TOGGLE
    dev.equwal.inkdim.ON
    dev.equwal.inkdim.OFF

Example with adb:

    adb shell am start -a dev.equwal.inkdim.TOGGLE

## Put it on a hardware button

[Rebind](https://github.com/equwal/rebind) remaps the buttons of an e-ink
reader or any Android device. It can put this toggle on a hardware button: bind
the button to the action `dev.equwal.inkdim.TOGGLE`. Rebind is from the same
maker.

More extensions: [Awesome Rebind](https://github.com/equwal/awesome-rebind).

## Say thanks

Ink Dim is free and open source. If it made your device better, you can
[buy me a coffee](https://ko-fi.com/truex).

## Build

You need JDK 17 or later and the Android SDK, with platform 36.

    ./gradlew testReleaseUnitTest assembleRelease

The APK is in `app/build/outputs/apk/release/`.

Release signing is optional. Put a `keystore.properties` file in the root of
the project with `storeFile`, `storePassword`, `keyAlias` and `keyPassword`.
Without that file the build makes an unsigned APK.

## Licence

GPL-3.0-or-later. See [LICENSE](LICENSE).

Copyright (c) 2026 equwal.
