plugins {
    id(Config.ApplyPlugins.ANDROID_LIBRARY)
    kotlin(Config.ApplyPlugins.Kotlin.ANDROID)
    id(Config.ApplyPlugins.KSP)
    id(Config.ApplyPlugins.PARCELIZE)
}

android {
    compileSdk = Config.AndroidSdkVersions.COMPILE_SDK

    defaultConfig {
        minSdk = Config.AndroidSdkVersions.MIN_SDK
        targetSdk = Config.AndroidSdkVersions.TARGET_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("proguard-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    buildFeatures {
        compose = true // Enables Jetpack Compose for this module
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Config.Compose.COMPOSE_COMPILER_VERSION
    }


//    buildTypes {
//        release {
//            minifyEnabled false
//            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
//        }
//    }


}

dependencies {
    // Accompanist
    accompanistDependencies()

    // AndroidX
    composeDependencies()

    // LaunchPad - Starting Assets
    launchPadDependencies()

    // Exo Player Library
    implementation("com.google.android.exoplayer:exoplayer:2.18.1")

//    todo - move to dependencies
    // Camera
    implementation("androidx.camera:camera-camera2:1.2.0-rc01")
    implementation("androidx.camera:camera-view:1.2.0-rc01")
    implementation("androidx.camera:camera-lifecycle:1.1.0")

    // Test
    junitDependencies()
    mockitoKotlinDependencies()
    truthDependencies()

}