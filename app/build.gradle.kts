plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "id.viasco.compassqstile"
    // Using API 35 (Android 15) as target
    compileSdk = 35 

    defaultConfig {
        applicationId = "id.viasco.compassqstile"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        
        // Remove test runner as we are not using AndroidX test libraries
        // testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" 
    }

    buildTypes {
        release {
            // VERY IMPORTANT: Enable code shrinking (R8)
            isMinifyEnabled = true
            // VERY IMPORTANT: Enable resource shrinking (remove unused resources)
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Minification disabled for debug to speed up compilation.
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // REMOVED ALL HEAVY UI DEPENDENCIES (AppCompat, Material, Core-KTX)
    // We only use the native Android Framework (android.*) and Kotlin Stdlib.
    
    // Note: If "class not found" errors occur for Kotlin classes, uncomment below:
    // implementation(platform(libs.kotlin.bom))
    // implementation(libs.kotlin.stdlib)
    
    // Currently empty to achieve minimal APK size.
}