# FamCart ProGuard Rules
# ========================

# Keep Firebase models (data classes used with Firebase Realtime Database)
-keepclassmembers class com.example.testing.models.** {
    *;
}

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }
-dontwarn com.google.firebase.auth.**

# Firebase Database
-keep class com.google.firebase.database.** { *; }
-dontwarn com.google.firebase.database.**

# Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Material Components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# AndroidX
-keep class androidx.** { *; }
-dontwarn androidx.**

# Keep application classes (prevent obfuscation of Activity names referenced in AndroidManifest)
-keep public class com.example.testing.** extends android.app.Activity
-keep public class com.example.testing.** extends androidx.appcompat.app.AppCompatActivity

# Keep adapters (RecyclerView adapters reference ViewHolder classes)
-keep class com.example.testing.adapters.** { *; }

# Preserve line number info for debugging
-keepattributes SourceFile,LineNumberTable

# Remove debug logging in release
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
}