plugins {
    id("personalmoneymanagement.android.feature")
}

dependencies {
    implementation(libs.findLibrary("androidx-activity-compose").get())
    implementation(project(":core:datastore"))
    implementation(project(":core:security"))
}
