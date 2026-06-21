# Keep your data transfer models completely intact from obfuscation alterations
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Protect Ktor and Supabase network structural endpoints
-keep class io.github.jan_tennert.supabase.** { *; }
-keep class io.ktor.** { *; }
