plugins {
    id("personalmoneymanagement.android.library")
    id("personalmoneymanagement.android.hilt")
}

android {
    namespace = "app.riyaspullur.personalmoneymanagement.core.ai"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.findLibrary("google-generativeai").get())
    implementation(libs.findLibrary("kotlinx-coroutines-core").get())

    testImplementation(libs.findLibrary("junit").get())
    testImplementation(libs.findLibrary("kotlinx-coroutines-test").get())
    testImplementation(libs.findLibrary("mockk").get())
}
