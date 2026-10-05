plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.newaye.finance"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.newaye.finance"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {

    // ---------------------------------------------------------
    // Jetpack Compose
    // ---------------------------------------------------------

    val composeBom =
        platform("androidx.compose:compose-bom:2025.08.01")

    implementation(composeBom)

    implementation("androidx.activity:activity-compose:1.13.0")

    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")

    debugImplementation(
        "androidx.compose.ui:ui-tooling"
    )
implementation(
    "androidx.lifecycle:lifecycle-viewmodel-compose:2.9.2"
)
    // ---------------------------------------------------------
    // AndroidX Core
    // ---------------------------------------------------------

    implementation("androidx.core:core-ktx:1.17.0")

    // ---------------------------------------------------------
    // Lifecycle
    // Required by collectAsStateWithLifecycle()
    // and lifecycle ViewModel Compose integration
    // ---------------------------------------------------------

    implementation(
        "androidx.lifecycle:lifecycle-runtime-compose:2.9.2"
    )

    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-compose:2.9.2"
    )

    // ---------------------------------------------------------
    // Room Database
    // ---------------------------------------------------------

    val roomVersion = "2.8.5"

    implementation(
        "androidx.room:room-runtime:$roomVersion"
    )

    implementation(
        "androidx.room:room-ktx:$roomVersion"
    )

    ksp(
        "androidx.room:room-compiler:$roomVersion"
    )
testImplementation("junit:junit:4.13.2")
}
