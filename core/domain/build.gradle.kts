plugins {
    id("personalmoneymanagement.jvm.library")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.findLibrary("kotlinx-serialization-json").get())
    implementation(libs.findLibrary("kotlinx-coroutines-core").get())
    implementation(libs.findLibrary("javax-inject").get())
}
