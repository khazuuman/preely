plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.preely"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.preely"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        debug {
            val MAPS_API_KEY = "AIzaSyDv7k0i13euDi8VBhsBSXy2TT-A7g0xBRg"
            buildConfigField("String", "MAPS_API_KEY", "\"${MAPS_API_KEY}\"")
            resValue("string", "google_maps_key", "${MAPS_API_KEY}")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val MAPS_API_KEY = "AIzaSyDv7k0i13euDi8VBhsBSXy2TT-A7g0xBRg"
            buildConfigField("String", "MAPS_API_KEY", "\"${MAPS_API_KEY}\"")
            resValue("string", "google_maps_key", "${MAPS_API_KEY}")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    implementation("io.minio:minio:8.5.8")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.swiperefreshlayout)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    annotationProcessor(libs.lombok)
    compileOnly(libs.lombok)
    implementation(libs.jbcrypt)
    implementation(libs.security.crypto)
    implementation(libs.gson)
    implementation(libs.shortcutbadger.leolin)
    implementation(libs.glide)
    implementation(libs.cloudinary.android)
    implementation(libs.picasso)
    implementation(libs.google.play.services.auth)
    implementation(libs.circle.image.view)
    implementation(libs.facebook.android.sdk)
    implementation(platform(libs.firebase.bom))
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.places)

}