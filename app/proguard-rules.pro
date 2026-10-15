# Reglas Proguard para Velvet VIP
-keep class com.google.firebase.** { *; }
-keep class com.google.protobuf.** { *; }
-keep class org.webrtc.** { *; }
-keep class io.getstream.** { *; }
-keep class com.example.my1eraapplication.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
-dontwarn org.webrtc.**
-dontwarn io.getstream.**
-dontwarn com.google.firebase.**
