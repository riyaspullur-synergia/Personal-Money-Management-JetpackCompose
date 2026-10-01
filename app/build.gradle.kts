plugins {
    id("personalmoneymanagement.android.application.compose")
    id("personalmoneymanagement.android.hilt")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "app.riyaspullur.personalmoneymanagement"

    defaultConfig {
        applicationId = "app.riyaspullur.personalmoneymanagement"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:data"))
    implementation(project(":core:common"))
    implementation(project(":core:security"))
    implementation(project(":core:ui"))
    implementation(project(":core:navigation"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:accounts"))
    implementation(project(":feature:dashboard"))
    implementation(project(":feature:reports"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:transactions"))
    implementation(project(":feature:assistant"))

    implementation(libs.findLibrary("androidx-activity-compose").get())
    implementation(libs.findLibrary("androidx-core-splashscreen").get())
    androidTestImplementation(libs.findLibrary("androidx-uiautomator").get())
}
