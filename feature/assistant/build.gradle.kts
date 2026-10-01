plugins {
    id("personalmoneymanagement.android.feature")
}

android {
    namespace = "app.riyaspullur.personalmoneymanagement.feature.assistant"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ai"))
    implementation(project(":core:ui"))
    implementation(project(":core:datastore"))
}
