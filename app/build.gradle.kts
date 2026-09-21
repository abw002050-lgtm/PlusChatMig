plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("com.google.gms.google-services") }
android { namespace = "com.chatmig.modern"; compileSdk = 35
 defaultConfig { applicationId = "com.chatmig.modern"; minSdk = 24; targetSdk = 35; versionCode = 27; versionName = "1.0.0-v27" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
dependencies {
 implementation(platform("androidx.compose:compose-bom:2024.12.01"))
 implementation("androidx.activity:activity-compose:1.10.0")
 implementation("androidx.compose.ui:ui")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.material:material-icons-extended")
 implementation("androidx.navigation:navigation-compose:2.8.5")
 implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
 implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
 implementation("com.google.firebase:firebase-auth:23.2.0")
 implementation("com.google.firebase:firebase-database:21.0.0")
 implementation("com.google.firebase:firebase-storage:21.0.1")
 implementation("com.google.firebase:firebase-messaging:24.1.0")
 implementation("io.coil-kt:coil-compose:2.7.0")
}
