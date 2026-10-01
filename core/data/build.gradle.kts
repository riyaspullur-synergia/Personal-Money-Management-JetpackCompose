plugins {
    id("personalmoneymanagement.android.library")
    id("personalmoneymanagement.android.hilt")
}

dependencies {
    implementation(libs.findLibrary("androidx-room-ktx").get())
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:export"))
}
