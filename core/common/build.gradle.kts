plugins {
    id("personalmoneymanagement.android.library")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.findLibrary("androidx-core-ktx").get())
}
