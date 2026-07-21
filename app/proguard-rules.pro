# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Room entities and DAOs are accessed via generated code + reflection for schema
# validation; keep annotated members so release builds don't strip them.
-keep class com.fielddevice.healthmonitor.data.local.entity.** { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# Keep Kotlin metadata so reflection-based libraries (Room, coroutines) keep working.
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions
-keep class kotlin.Metadata { *; }
