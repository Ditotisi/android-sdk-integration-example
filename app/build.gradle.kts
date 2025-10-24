
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.sdkverihubs"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.sdkverihubs"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val licenseId = rootProject.findProperty("LICENSE_ID") as? String
            ?: error("LICENSE_ID not defined")

        val licenseKey = rootProject.findProperty("LICENSE_KEY") as? String
            ?: error("LICENSE_KEY not defined")

        val baseProxyURL = rootProject.findProperty("BASE_PROXY_URL") as? String
            ?: error("BASE_PROXY_URL not defined")

        buildConfigField(
            "String",
            "LICENSE_ID",
            "\"${licenseId}\""
        )
        buildConfigField(
            "String",
            "LICENSE_KEY",
            value = "\"${licenseKey}\""
        )
        buildConfigField(
            "String",
            "BASE_PROXY_URL",
            value = "\"${baseProxyURL}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
    implementation("com.google.android.material:material:1.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.0")
    implementation("com.github.bumptech.glide:glide:4.9.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}