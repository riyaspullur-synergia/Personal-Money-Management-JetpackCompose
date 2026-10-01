plugins {
    id("personalmoneymanagement.android.library")
    id("personalmoneymanagement.android.hilt")
    alias(libs.plugins.protobuf)
}

android {
    namespace = "app.riyaspullur.personalmoneymanagement.core.datastore"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(libs.findLibrary("androidx-datastore-preferences").get())
    implementation(libs.findLibrary("androidx-datastore-proto").get())
    implementation(libs.findLibrary("protobuf-kotlin-lite").get())
}

protobuf {
    protoc {
        artifact = libs.findLibrary("protobuf-protoc").get().get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                register("java") {
                    option("lite")
                }
                register("kotlin") {
                    option("lite")
                }
            }
        }
    }
}
