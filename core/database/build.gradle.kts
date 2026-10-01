plugins {
    id("personalmoneymanagement.android.library")
    id("personalmoneymanagement.android.hilt")
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.findLibrary("androidx-room-runtime").get())
    implementation(libs.findLibrary("androidx-room-ktx").get())
    ksp(libs.findLibrary("androidx-room-compiler").get())
}
