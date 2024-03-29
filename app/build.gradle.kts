@file:SuppressWarnings("INCUBATING")

import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.android.tools.build.gradle)
    kotlin("android")
    kotlin("kapt")
    alias(libs.plugins.hilt.android.gradle)
    kotlin("plugin.serialization") version libs.versions.kotlin
}

android {
    namespace = "net.inferno.socialmedia"

    signingConfigs {
        maybeCreate("testKey").apply {
            keyAlias = "key0"
            keyPassword = "123456"
            storeFile = file("../testkey.jks")
            storePassword = "123456"
        }
    }

    buildToolsVersion = "34.0.0"
    compileSdk = 34
//    compileSdkExtension = 4

    defaultConfig {
        applicationId = "net.inferno.socialmedia"

        compileSdkPreview = "UpsideDownCake"
        minSdk = 24
        targetSdk = 34

        versionCode = 1
        versionName = "1.0"

        vectorDrawables.useSupportLibrary = true

        buildFeatures {
            compose = true
            buildConfig = true
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += arrayOf("dev")

    productFlavors {
        maybeCreate("dev").apply {
            dimension = "dev"

            minSdk = 31

            versionNameSuffix =
                "-dev-${DateTimeFormatter.ofPattern("dd-MM-yyyy").format(LocalDate.now())}"

            buildConfigField("boolean", "DEV", "true")
            resourceConfigurations += arrayOf("en", "xxhdpi")
        }

        maybeCreate("deploy").apply {
            dimension = "dev"

            minSdk = 24

            buildConfigField("boolean", "DEV", "false")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
        }

        maybeCreate("preRelease").apply {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = true

            versionNameSuffix =
                "-${DateTimeFormatter.ofPattern("dd-MM-yyyy").format(LocalDate.now())}"

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs["testKey"]
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs["testKey"]
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = freeCompilerArgs + arrayOf(
            "-Xskip-prerelease-check",
            "-Xopt-in=kotlin.RequiresOptIn",
        )
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

dependencies {
    //region Kotlin
    implementation(libs.kotlin.std)
    implementation(libs.kotlinx.coroutines.android)
    //endregion

    //region AndroidX
    implementation(libs.androidx.core)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.preferences)
    implementation(libs.androidx.datastore.preferences)
    //endregion

    //region Hilt
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)
    //endregion

    //region Google
    implementation(libs.google.material)
    implementation(platform("com.google.firebase:firebase-bom:32.2.2"))

    implementation("com.google.firebase:firebase-firestore-ktx")
    //endregion

    //region Networking
    implementation(libs.square.okhttp)
    implementation(libs.square.okhttp.logginginterceptor)

    implementation(libs.square.retrofit)
    implementation(libs.square.retrofit.moshi)

    implementation(libs.square.moshi)
    implementation(libs.square.moshi.adapters)
    implementation(libs.square.moshi.kotlin)
    kapt(libs.square.moshi.kotlin.codegen)

    implementation(libs.coil.compose)
    implementation(libs.touchimageview)
    api(libs.imagecropper)
    //endregion

    //region Compose
    implementation(libs.bundles.compose)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.service)

    implementation(libs.androidx.compose.material3)
    //endregion

    coreLibraryDesugaring(libs.desugar.jdk.libs)
    debugImplementation(libs.androidx.compose.ui.tooling)

    //region Privacy Sandbox
    implementation(libs.androidx.privacysandbox.ads.adservices)

//    implementation(libs.androidx.privacysandbox.ui)
//    implementation(libs.androidx.privacysandbox.ui.core)
//    implementation(libs.androidx.privacysandbox.ui.ui.provider)

//    implementation(libs.androidx.privacysandbox.sdkruntime.core)
//    implementation(libs.androidx.privacysandbox.sdkruntime.client)

//    implementation(libs.androidx.privacysandbox.tools)
//    implementation(libs.androidx.privacysandbox.tools.datasource)
    //endregion

    //region Third Party
    implementation(libs.bundles.commonmark)
    //endregion

    //region Testing
    // Test rules and transitive dependencies:
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    // Needed for createAndroidComposeRule, but not createComposeRule:
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation("androidx.tracing:tracing:1.1.0")
    //endregion
}