plugins {
    id("personalmoneymanagement.android.feature")
}

dependencies {
    implementation(project(":core:datastore"))
    implementation(project(":core:data"))
}
