plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.nocturnal"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.nocturnal"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-base:18.2.0")


    implementation(libs.androidx.core.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1") // Check for the latest version
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("com.mapbox.maps:android:11.7.1")
    implementation("com.mapbox.extension:maps-compose:11.7.1")


    // implementation("com.mapbox.maps:plugin-locationcomponent:11.6.0")
    implementation("com.karumi:dexter:6.2.3")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.espresso.core)
    testImplementation(libs.junit)
    testImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.navigation.compose)

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-storage-ktx:12.3.3")


    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")

    implementation("androidx.navigation:navigation-compose:2.6.0")

    implementation ("androidx.fragment:fragment-ktx:1.6.1")

    implementation ("com.google.firebase:firebase-firestore")

    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")

    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")

    implementation ("com.google.firebase:firebase-auth")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")

    implementation ("androidx.compose.runtime:runtime:1.0.5")
    // Core Compose UI components
    implementation("androidx.compose.ui:ui:1.5.0") // Core UI components for Compose

    // Material Design components for Compose (includes Scaffold, BottomNavigation)
    implementation("androidx.compose.material:material:1.5.0")

    // Optional: Tooling support (for previewing composables in Android Studio)
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.0")

    // Navigation for Compose
    implementation("androidx.navigation:navigation-compose:2.6.0")

    // Additional dependencies (if not already added)
    implementation("androidx.activity:activity-compose:1.7.2") // Required for setContent in Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1") // Lifecycle support for Compose
    implementation("androidx.compose.runtime:runtime-livedata:1.6.0")

    androidTestImplementation ("androidx.test:rules:1.5.0")

    testImplementation ("junit:junit:4.13.2")
    testImplementation ("org.jetbrains.kotlin:kotlin-test-junit:1.9.10")

    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation ("org.mockito:mockito-core:4.11.0")
    testImplementation ("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation ("junit:junit:4.13.2")

    testImplementation ("org.mockito:mockito-inline:4.11.0")

    testImplementation ("net.bytebuddy:byte-buddy:1.14.7")

    testImplementation ("org.robolectric:robolectric:4.10")

    // JUnit for unit testing
    testImplementation ("junit:junit:4.13.2")

    // Mockito for mocking
    testImplementation ("org.mockito:mockito-core:5.5.0")

    // AndroidX Testing for ViewModel and LiveData
    testImplementation ("androidx.arch.core:core-testing:2.1.0")

    // Kotlin Coroutines Testing
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries
}