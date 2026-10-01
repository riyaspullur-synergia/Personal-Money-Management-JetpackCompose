plugins {
    id("personalmoneymanagement.android.feature")
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:security"))
}
