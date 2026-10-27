import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Release signing credentials live outside the source tree (keystore/upload-keystore
// .properties, gitignored) so the passwords are never committed. Debug builds and any
// build that doesn't need to be installable-from-a-store keep working without them -
// release signing is simply skipped, matching AGP's default unsigned-release behavior.
val uploadKeystoreProperties = Properties().apply {
    val propertiesFile = rootProject.file("keystore/upload-keystore.properties")
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use { load(it) }
    }
}
val hasUploadKeystore = uploadKeystoreProperties.getProperty("UPLOAD_STORE_FILE") != null

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.myowndime.duoscoreboard"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.myowndime.duoscoreboard"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasUploadKeystore) {
            create("upload") {
                storeFile = rootProject.file(
                    "keystore/${uploadKeystoreProperties.getProperty("UPLOAD_STORE_FILE")}",
                )
                storePassword = uploadKeystoreProperties.getProperty("UPLOAD_STORE_PASSWORD")
                keyAlias = uploadKeystoreProperties.getProperty("UPLOAD_KEY_ALIAS")
                keyPassword = uploadKeystoreProperties.getProperty("UPLOAD_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (hasUploadKeystore) {
                signingConfig = signingConfigs.getByName("upload")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    // The exported Room schemas double as the fixture the migration test replays.
    sourceSets.getByName("test") {
        resources.srcDir("$projectDir/schemas")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        abortOnError = true
        warningsAsErrors = true
        checkDependencies = true
        // Version-freshness checks require network access and are unrelated to app quality.
        disable += setOf("GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion")
        // OldTargetApi wants a platform newer than 36, but 36 (Android 16) is the newest
        // platform the SDK manager publishes and the level Google Play requires for new
        // apps. Re-enable this check as soon as a newer stable platform ships.
        disable += "OldTargetApi"
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

// The Robolectric UI tests drive the host activity that androidx's ui-test-manifest
// injects into the debug manifest only. The release unit test variant would run the
// very same JVM code without that activity, so it is switched off rather than
// duplicated - `testDebugUnitTest` is the single source of truth for unit tests.
tasks.matching { it.name == "testReleaseUnitTest" }.configureEach {
    enabled = false
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
