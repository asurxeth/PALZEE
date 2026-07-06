plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    kotlin("plugin.serialization") version "2.0.0"
}

import java.util.Properties

val envProperties = Properties().apply {
    val envFile = project.rootProject.file(".env")
    if (envFile.exists()) {
        envFile.inputStream().use { load(it) }
    }
}

val localProperties = Properties().apply {
    val localFile = project.rootProject.file("local.properties")
    if (localFile.exists()) {
        localFile.inputStream().use { load(it) }
    }
}

val supabaseUrl = (envProperties.getProperty("SUPABASE_URL") ?: System.getenv("SUPABASE_URL") ?: "https://placeholder-url.supabase.co").trim('"')
val supabaseAnonKey = (envProperties.getProperty("SUPABASE_ANON_KEY") ?: System.getenv("SUPABASE_ANON_KEY") ?: "placeholder-anon-key").trim('"')
val googleWebClientId = (localProperties.getProperty("google.web.client.id") ?: envProperties.getProperty("GOOGLE_WEB_CLIENT_ID") ?: System.getenv("GOOGLE_WEB_CLIENT_ID") ?: "").trim('"')

android {
    namespace = "com.finrein.pals"
    compileSdk = 34

    signingConfigs {
        create("release") {
            storeFile = project.rootProject.file("Palls.jks")
            storePassword = "11223344"
            keyAlias = "pals-release"
            keyPassword = "11223344"
        }
    }

    defaultConfig {
        applicationId = "com.finrein.pals"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")

        javaCompileOptions {
            annotationProcessorOptions {
                argument("dagger.hilt.android.internal.disableAndroidSuperclassValidation", "true")
            }
        }
        ksp {
            arg("dagger.hilt.android.internal.disableAndroidSuperclassValidation", "true")
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
        }
        getByName("release") {
            isMinifyEnabled = false      // Turn this OFF to stop R8 from stripping code
            isShrinkResources = false   // Turn this OFF to keep all assets intact
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release") // Keeps your working Palls.jks signature
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}


dependencies {
    // Core AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose BOM & UI Elements
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.ui.text.google.fonts)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Room local storage
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore & Crypto
    implementation(libs.datastore.preferences)
    implementation(libs.security.crypto.ktx)

    // Ktor client & network capabilities
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.websockets)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.gotrue)
    implementation(libs.supabase.compose.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.realtime)

    // Core Credential Manager Library
    implementation("androidx.credentials:credentials:1.2.2")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // CameraX dependencies
    val cameraVersion = "1.4.0"
    implementation("androidx.camera:camera-core:$cameraVersion")
    implementation("androidx.camera:camera-camera2:$cameraVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraVersion")
    implementation("androidx.camera:camera-video:$cameraVersion")
    implementation("androidx.camera:camera-view:$cameraVersion")

    // Media3 ExoPlayer dependencies for loop playback in preview
    val media3Version = "1.3.1"
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-ui:$media3Version")

    // Tooling dependencies for development
    debugImplementation(libs.androidx.compose.ui.tooling)
}



tasks.withType<JavaCompile>().configureEach {
    options.isWarnings = true
    options.compilerArgs.addAll(
        listOf(
            "-Xlint:deprecation",
            "-Xlint:unchecked"
        )
    )
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}

tasks.register("readCrash") {
    doLast {
        val destFile = file("${projectDir}/../crash_log_real.txt")
        try {
            val process = ProcessBuilder("adb", "logcat", "-d", "-b", "crash")
                .redirectOutput(ProcessBuilder.Redirect.to(destFile))
                .start()
            process.waitFor()
            println("Successfully read crash log and wrote to: ${destFile.absolutePath}")
        } catch (e: Exception) {
            println("Error reading crash log: ${e.message}")
        }
    }
}

tasks.register("readLogcat") {
    doLast {
        val destFile = file("${projectDir}/../logcat_real.txt")
        try {
            // Retrieve logcat with main, system, and crash buffers
            val process = ProcessBuilder("adb", "logcat", "-d")
                .redirectOutput(ProcessBuilder.Redirect.to(destFile))
                .start()
            process.waitFor()
            println("Successfully read logcat and wrote to: ${destFile.absolutePath}")
        } catch (e: Exception) {
            println("Error reading logcat: ${e.message}")
        }
    }
}


