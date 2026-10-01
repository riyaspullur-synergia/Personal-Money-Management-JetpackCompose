plugins {
    id("personalmoneymanagement.android.library.compose")
    id("personalmoneymanagement.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:ui"))
    implementation(project(":core:security"))
    implementation(project(":core:domain"))

    implementation(project(":feature:auth"))
    implementation(project(":feature:accounts"))
    implementation(project(":feature:dashboard"))
    implementation(project(":feature:reports"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:transactions"))
    implementation(project(":feature:assistant"))

    implementation(libs.findLibrary("androidx-navigation-compose").get())
    implementation(libs.findLibrary("androidx-hilt-navigation-compose").get())
    implementation(libs.findLibrary("kotlinx-serialization-json").get())
    implementation(libs.findLibrary("androidx-material-icons-extended").get())
    implementation(libs.findLibrary("androidx-lifecycle-runtime-ktx").get())
}
