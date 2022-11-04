//apply plugin: 'com.google.gms.google-services'
//apply plugin: 'com.google.ar.sceneform.plugin'

plugins {
    id(Config.ApplyPlugins.ANDROID_APPLICATION)
    kotlin(Config.ApplyPlugins.Kotlin.ANDROID)
    id(Config.ApplyPlugins.KSP)
    id(Config.ApplyPlugins.PARCELIZE)
}

BuildInfoManager.initialize(
    BuildInfoInput(
        appVersion = AppVersion(major = 1, minor = 0, patch = 0, hotfix = 0, showEmptyPatchNumberInVersionName = true), // TODO: TEMPLATE - Replace with appropriate app version
        brandName = "PathFinder",
        productionReleaseVariantName = "productionRelease",
        rootProjectDir = rootDir
    )
)

android {
    compileSdk = Config.AndroidSdkVersions.COMPILE_SDK
    buildToolsVersion = Config.AndroidSdkVersions.BUILD_TOOLS

    defaultConfig {
        minSdk = Config.AndroidSdkVersions.MIN_SDK
        targetSdk = Config.AndroidSdkVersions.TARGET_SDK
        versionCode = BuildInfoManager.APP_VERSION.versionCode
        versionName = BuildInfoManager.APP_VERSION.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        compose =  true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Config.Compose.COMPOSE_COMPILER_VERSION
    }

    androidResources {
        noCompress ("tflite")
    }

    signingConfigs {
        create("release") {
            // Release keystore expected to be present in environment variables (living on the build server)
            storeFile = file(System.getenv("_KEYSTORE") ?: "_KEYSTORE environment variable not set for release build type; unable to compile the current variant")
            storePassword = System.getenv("_KEYSTORE_PASSWORD")
            keyAlias = System.getenv("_KEY_ALIAS")
            keyPassword = System.getenv("_KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

}

dependencies {
    implementation(project(mapOf("path" to ":domain")))
    implementation(project(mapOf("path" to ":compose")))

    // Kotlin/coroutines
    kotlinDependencies()
    coroutineDependencies()

    // Koin DI
    koinAndroidDependencies()

    // AndroidX
    composeDependencies()
    accompanistDependencies()
    appCompatDependencies()
    activityDependencies()
    androidxStartupDependencies()
    materialDependencies()
    lifecycleDependencies()
    navigationDependencies()

    // Core Desugaring
    coreLibraryDesugaringDependencies()

    // Testing
    junitDependencies()
    mockitoKotlinDependencies()
    truthDependencies()
    archCoreTestingDependencies()
    kotlinxCoroutineTestingDependencies()
}

//apply plugin: 'com.google.gms.google-services'
