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

val supabaseUrl = (envProperties.getProperty("SUPABASE_URL") ?: System.getenv("SUPABASE_URL") ?: "https://placeholder-url.supabase.co").trim('"')
val supabaseAnonKey = (envProperties.getProperty("SUPABASE_ANON_KEY") ?: System.getenv("SUPABASE_ANON_KEY") ?: "placeholder-anon-key").trim('"')
val googleWebClientId = (envProperties.getProperty("GOOGLE_WEB_CLIENT_ID") ?: System.getenv("GOOGLE_WEB_CLIENT_ID") ?: "placeholder-google-client-id").trim('"')

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
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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

tasks.register("copySmileyAvatar") {
    doLast {
        val srcFile = file("/Users/pratham/.gemini/antigravity-ide/brain/58f8b2fa-06c6-4949-8a11-2bdbc26f1b3d/media__1781426797065.png")
        val destFile = file("${projectDir}/src/main/res/drawable/custom_profile_icon.png")
        if (srcFile.exists()) {
            srcFile.copyTo(destFile, overwrite = true)
            println("Successfully copied custom profile icon PNG!")
        } else {
            println("Source profile icon PNG not found at: ${srcFile.absolutePath}")
        }

        val srcFile2 = file("/Users/pratham/.gemini/antigravity-ide/brain/58f8b2fa-06c6-4949-8a11-2bdbc26f1b3d/media__1781426803102.png")
        val destFile2 = file("${projectDir}/src/main/res/drawable/custom_rotate_smiley.png")
        if (srcFile2.exists()) {
            srcFile2.copyTo(destFile2, overwrite = true)
            println("Successfully copied custom rotate smiley PNG!")
        } else {
            println("Source rotate smiley PNG not found at: ${srcFile2.absolutePath}")
        }

        val srcFile3 = file("/Users/pratham/.gemini/antigravity-ide/brain/58f8b2fa-06c6-4949-8a11-2bdbc26f1b3d/media__1781522628764.png")
        val destFile3 = file("${projectDir}/src/main/res/drawable/ic_checkmark_custom.png")
        if (srcFile3.exists()) {
            srcFile3.copyTo(destFile3, overwrite = true)
            println("Successfully copied custom checkmark icon PNG!")
        } else {
            println("Source checkmark icon PNG not found at: ${srcFile3.absolutePath}")
        }

        val srcFile4 = file("/Users/pratham/.gemini/antigravity-ide/brain/58f8b2fa-06c6-4949-8a11-2bdbc26f1b3d/media__1781544652508.png")
        val destFile4 = file("${projectDir}/src/main/res/drawable/ic_flash_off.png")
        if (srcFile4.exists()) {
            srcFile4.copyTo(destFile4, overwrite = true)
            println("Successfully copied custom flash off icon PNG!")
        } else {
            println("Source flash off icon PNG not found at: ${srcFile4.absolutePath}")
        }

        val logoSrc = file("/Users/pratham/.gemini/antigravity-ide/brain/43c694e0-5643-4328-b7d2-4e82f551337e/media__1781903965903.jpg")
        val logoDest = file("${projectDir}/src/main/res/drawable/ic_launcher_custom.jpg")
        val logoOldDest = file("${projectDir}/src/main/res/drawable/ic_launcher_custom.png")
        if (logoSrc.exists()) {
            logoSrc.copyTo(logoDest, overwrite = true)
            println("Successfully copied custom launcher logo JPG!")
            if (logoOldDest.exists()) {
                logoOldDest.delete()
                println("Deleted old custom launcher logo PNG.")
            }
        } else {
            println("Source launcher logo JPG not found at: ${logoSrc.absolutePath}")
        }
    }
}

tasks.register("clearBrainArtifacts") {
    doLast {
        val brainDir = file("/Users/pratham/.gemini/antigravity-ide/brain/58f8b2fa-06c6-4949-8a11-2bdbc26f1b3d")
        if (brainDir.exists()) {
            brainDir.listFiles()?.forEach { f ->
                if (f.name.startsWith("media_") || f.name.endsWith(".png") || f.name.endsWith(".jpg") || f.name.endsWith(".mp4")) {
                    f.deleteRecursively()
                }
            }
            val tempMedia = file("/Users/pratham/.gemini/antigravity-ide/brain/0fdceaff-b38b-4424-ab1f-518a93f39a23/.tempmediaStorage")
            if (tempMedia.exists()) {
                tempMedia.deleteRecursively()
            }
            println("Cleared brain artifacts!")
        }
    }
}

tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn("copySmileyAvatar")
    finalizedBy("clearBrainArtifacts")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:-deprecation")
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


