-dontwarn org.slf4j.impl.StaticLoggerBinder

# --- Keep Kotlinx Serialization annotations and classes ---
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep serialization helper classes
-keepclassmembers class com.finrein.pals.** {
    *** Companion;
    *** $serializer;
}

# Keep all classes annotated with @Serializable
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Keep all domain data models since they are serialization DTOs mapped from Supabase
-keep class com.finrein.pals.core.domain.model.** { *; }
-keep class com.finrein.pals.core.data.repository.** { *; }

# --- Supabase Kotlin SDK ---
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**

# --- Ktor Client rules ---
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# --- Google Credentials API for Google Sign-in ---
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn androidx.credentials.**
-dontwarn com.google.android.libraries.identity.googleid.**

