# Keep line numbers for readable crash reports (no crash reporting SDK is used;
# this only helps when reading a stack trace pulled manually from logcat).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room generates implementations reflectively referenced by the runtime.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**
