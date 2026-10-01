plugins {
    id("personalmoneymanagement.android.library.compose")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.findLibrary("androidx-core-ktx").get())
    implementation(libs.findLibrary("androidx-core-splashscreen").get())
    implementation(libs.findLibrary("androidx-material-icons-extended").get())
}
