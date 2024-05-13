plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.esidev.fakeit"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.esidev.fakeit"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        mlModelBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.tensorflow.lite.metadata)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("org.tensorflow:tensorflow-lite:0.0.0-nightly-SNAPSHOT")
    implementation ("org.tensorflow:tensorflow-lite-gpu:0.0.0-nightly-SNAPSHOT")
    implementation ("org.tensorflow:tensorflow-lite-support:0.0.0-nightly-SNAPSHOT")

}