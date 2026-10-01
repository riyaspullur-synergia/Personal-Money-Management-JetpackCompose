plugins {
    id("personalmoneymanagement.android.library")
    id("personalmoneymanagement.android.hilt")
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(libs.findLibrary("kotlinx-serialization-json").get())
}
