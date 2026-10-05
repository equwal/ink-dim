# Changelog

## 0.1.4

- The app names Shizuku in its package-visibility list. Without it, Android 11
  and later hid Shizuku from the app, so the settings screen never offered the
  permission step and the toggle could not run.

## 0.1.3

- The app declares the Shizuku provider, so Shizuku can hand it the shell.
- The description says that the toggle may need `su`.

## 0.1.2

- The signed APK has no dependency list for Google in it, so F-Droid can check that its own build is the same.

## 0.1.1

- The APK is smaller: R8 removes the code that the app does not use.

## 0.1.0

First release.

- Tap the icon to set the frontlight below the lowest level of the system.
- Tap the icon again to go back to the system level.
- A quick settings tile, "Extra dim", with the state.
- The intent actions `dev.equwal.inkdim.TOGGLE`, `dev.equwal.inkdim.ON` and
  `dev.equwal.inkdim.OFF`.
- A "Settings" shortcut on a long press of the icon. It shows the state of
  Shizuku and the steps to set it up.
