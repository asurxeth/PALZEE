plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.android.library")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.0.0"
}

kotlin {
    applyDefaultHierarchyTemplate()
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                
                // Supabase Multiplatform SDK (included via BOM or direct)
                implementation(libs.supabase.gotrue)
                implementation(libs.supabase.postgrest)
                implementation(libs.supabase.storage)
                implementation(libs.supabase.realtime)
                
                // Multiplatform Settings
                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.no.arg)
                
                // Ktor Multiplatform Client
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.websockets)
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core.ktx)
                
                // CameraX dependencies
                val cameraVersion = "1.4.2"
                implementation("androidx.camera:camera-core:$cameraVersion")
                implementation("androidx.camera:camera-camera2:$cameraVersion")
                implementation("androidx.camera:camera-lifecycle:$cameraVersion")
                implementation("androidx.camera:camera-video:$cameraVersion")
                implementation("androidx.camera:camera-view:$cameraVersion")
                implementation("androidx.camera:camera-extensions:$cameraVersion")

                // Media3 ExoPlayer dependencies
                val media3Version = "1.4.0"
                implementation("androidx.media3:media3-exoplayer:$media3Version")
                implementation("androidx.media3:media3-ui:$media3Version")
                implementation("androidx.media3:media3-common:$media3Version")
            }
        }
        
        val iosMain by getting {
            dependencies {
            }
        }
    }
}

android {
    namespace = "com.finrein.pals.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 29
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
