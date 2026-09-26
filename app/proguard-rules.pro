# Shell.kt calls the private method Shizuku.newProcess by reflection.
# Keep its name, so that R8 does not remove or rename it.
-keepclassmembers class rikka.shizuku.Shizuku {
    *** newProcess(java.lang.String[], java.lang.String[], java.lang.String);
}
