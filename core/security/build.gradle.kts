plugins {
    id("personalmoneymanagement.android.library")
    id("personalmoneymanagement.android.hilt")
}

dependencies {
    implementation(libs.findLibrary("androidx-biometric-ktx").get())
}
