import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.dsl.DependencyHandler


// TODO: Update corresponding buildSrc/build.gradle.kts value when updating this version!
private const val KOTLIN_VERSION = "1.7.0"
private const val KOTLIN_COROUTINES_VERSION = "1.6.0"

/**
 * Provides the source of truth for version/configuration information to any gradle build file (project and app module build.gradle.kts)
 */
object Config {
    const val KTLINT_GRADLE_VERSION = "10.2.1"
    const val KTLINT_VERSION = "0.43.2"
    const val KSP_VERSION = "1.7.0-1.0.6"

    /**
     * Called from root project buildscript block in the project root build.gradle.kts
     */
    object BuildScriptPlugins {
        // TODO: Update corresponding buildSrc/build.gradle.kts value when updating this version!
        const val ANDROID_GRADLE = "com.android.tools.build:gradle:7.2.2"
        const val KOTLIN_GRADLE = "org.jetbrains.kotlin:kotlin-gradle-plugin:$KOTLIN_VERSION"
        const val GRADLE_VERSIONS = "com.github.ben-manes:gradle-versions-plugin:0.41.0"
    }

    /**
     * Called in non-root project modules via id()[org.gradle.kotlin.dsl.PluginDependenciesSpecScope.id]
     * or kotlin()[org.gradle.kotlin.dsl.kotlin] (the PluginDependenciesSpec.kotlin extension function) in a build.gradle.kts
     */
    object ApplyPlugins {
        const val ANDROID_APPLICATION = "com.android.application"
        const val ANDROID_LIBRARY = "com.android.library"
        const val GRADLE_VERSIONS = "com.github.ben-manes.versions"
        const val KT_LINT = "org.jlleitschuh.gradle.ktlint"
        const val PARCELIZE = "kotlin-parcelize"
        const val KSP = "com.google.devtools.ksp"
        object Kotlin {
            const val ANDROID = "android"
        }
    }

    object AndroidSdkVersions {
        const val COMPILE_SDK = 33
        const val BUILD_TOOLS = "31.0.0"
        const val MIN_SDK = 24
        const val TARGET_SDK = 31
    }

    object Compose {
        const val COMPOSE_VERSION = "1.2.0-rc03"
        const val COMPOSE_COMPILER_VERSION = "1.2.0"
    }

}

/**
 * Dependency Version definitions with links to source (if open source)/release notes. Defines the version in one place for multiple dependencies that use the same version.
 * Use [DependencyHandler] extension functions below that provide logical groupings of dependencies including appropriate configuration accessors.
 */
private object Libraries {
    //// AndroidX
    const val CORE_KTX = "androidx.core:core-ktx:1.7.0"
    const val APP_COMPAT = "androidx.appcompat:appcompat:1.4.1"
    const val STARTUP = "androidx.startup:startup-runtime:1.1.0"

    // Lifecycle
    private const val LIFECYCLE_VERSION = "2.4.0"
    const val LIFECYCLE_LIVEDATA_KTX =
        "androidx.lifecycle:lifecycle-livedata-ktx:$LIFECYCLE_VERSION"
    const val LIFECYCLE_COMPOSE =
        "androidx.lifecycle:lifecycle-viewmodel-compose:$LIFECYCLE_VERSION"


    // Compose
    const val COMPOSE_ACTIVITY = "androidx.activity:activity-compose:1.4.0"

    private const val COMPOSE_VERSION = Config.Compose.COMPOSE_VERSION
    private const val COMPOSE_COMPILER_VERSION = Config.Compose.COMPOSE_COMPILER_VERSION
    const val COMPOSE_COMPILER = "androidx.compose.compiler:compiler:$COMPOSE_COMPILER_VERSION"
    const val COMPOSE_UI = "androidx.compose.ui:ui:$COMPOSE_VERSION"

    // Tooling support (Previews, etc.)
    const val COMPOSE_UI_TOOLING = "androidx.compose.ui:ui-tooling:$COMPOSE_VERSION"
    const val COMPOSE_UI_TOOLING_PREVIEW = "androidx.compose.ui:ui-tooling-preview:$COMPOSE_VERSION"

    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    const val COMPOSE_FOUNDATION = "androidx.compose.foundation:foundation:$COMPOSE_VERSION"
    const val COMPOSE_ANIMATION = "androidx.compose.animation:animation:$COMPOSE_VERSION"

    // Navigation
    const val NAVIGATION_COMPOSE = "androidx.navigation:navigation-compose:2.4.0-rc01"

    // Material Design
    const val COMPOSE_MATERIAL = "androidx.compose.material:material:$COMPOSE_VERSION"

    // Material 3 - Window Size Class
    const val COMPOSE_MATERIAL3_WINDOW_SIZE =
        "androidx.compose.material3:material3-window-size-class:1.0.0-alpha13"

    // Material design icons
    const val COMPOSE_MATERIAL_ICONS_CORE =
        "androidx.compose.material:material-icons-core:$COMPOSE_VERSION"
    const val COMPOSE_MATERIAL_ICONS_EXTENDED =
        "androidx.compose.material:material-icons-extended:$COMPOSE_VERSION"

    // Integration with observables
    const val COMPOSE_LIVE_DATA = "androidx.compose.runtime:runtime-livedata:$COMPOSE_VERSION"

    // Launchpad
    const val LAUNCHPAD_COMPOSE = "com.github.BottleRocketStudios:Android-LaunchPad-Compose:0.4.1"

    // Accompanist
    private const val ACCOMPANIST_VERSION = "0.24.5-alpha"
    const val ACCOMPANIST_WEBVIEW =
        "com.google.accompanist:accompanist-webview:$ACCOMPANIST_VERSION"
    const val ACCOMPANIST_NAVIGATION_ANIMATION =
        "com.google.accompanist:accompanist-navigation-animation:$ACCOMPANIST_VERSION"
    const val ACCOMPANIST_PERMISSIONS =
        "com.google.accompanist:accompanist-permissions:$ACCOMPANIST_VERSION"


//    // Coil
//    // https://coil-kt.github.io/coil/
//    // https://coil-kt.github.io/coil/changelog/#full-release-notes
//    private const val COIL_VERSION = "2.0.0-rc02"
//    const val COIL = "io.coil-kt:coil:$COIL_VERSION"
//    const val COIL_COMPOSE_EXT = "io.coil-kt:coil-compose:$COIL_VERSION"

    //// Material
    // https://github.com/material-components/material-components-android/releases
    const val MATERIAL = "com.google.android.material:material:1.5.0"

    // https://github.com/material-components/material-components-android-compose-theme-adapter/releases/
    // Navigate to above link, search for latest material-vX.Y.Z that supports a matching version of Compose, and just use the X.Y.Z in the dependency version below
    const val MATERIAL_COMPOSE_THEME_ADAPTER =
        "com.google.android.material:compose-theme-adapter:1.1.3"

    //// Kotlin
    const val KOTLIN_STDLIB_JDK7 = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$KOTLIN_VERSION"
    const val KOTLIN_REFLECT = "org.jetbrains.kotlin:kotlin-reflect:$KOTLIN_VERSION"

    //// Coroutines + Flow
    const val KOTLINX_COROUTINES_CORE =
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:$KOTLIN_COROUTINES_VERSION"
    const val KOTLINX_COROUTINES_ANDROID =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:$KOTLIN_COROUTINES_VERSION"

    // Koin
    const val KOIN_KOTLIN = "io.insert-koin:koin-core:3.2.2"
    const val KOIN_ANDROID = "io.insert-koin:koin-android:3.2.3"

    // Core desugaring
    const val CORE_LIBRARY_DESUGARING = "com.android.tools:desugar_jdk_libs:1.1.5"

    // Commons codec - used for base64 operations (no android framework requirement)
    const val COMMONS_CODEC = "commons-codec:commons-codec:1.15"

    // Chucker
    private const val CHUCKER_VERSION = "3.5.2"
    const val CHUCKER = "com.github.ChuckerTeam.Chucker:library:$CHUCKER_VERSION"
    const val CHUCKER_NO_OP = "com.github.ChuckerTeam.Chucker:library-no-op:$CHUCKER_VERSION"
}

/**
 * test and/or androidTest specific dependencies.
 * Use [DependencyHandler] extension functions below that provide logical groupings of dependencies including appropriate configuration accessors.
 */
private object TestLibraries {
    const val JUNIT = "junit:junit:4.13.2"
    const val TRUTH = "com.google.truth:truth:1.1.3"
    const val MOCKITO_KOTLIN = "org.mockito.kotlin:mockito-kotlin:4.0.0"

    //// AndroidX - testing
    const val ARCH_CORE_TESTING = "androidx.arch.core:core-testing:2.1.0"
    const val ANDROIDX_TEST_CORE = "androidx.test:core:1.4.0"
    const val ANDROIDX_TEST_CORE_KTX = "androidx.test:core-ktx:1.4.0"

    //// Kotlinx Coroutine - Testing
    const val KOTLINX_COROUTINE_TESTING = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$KOTLIN_COROUTINES_VERSION"

    // Turbine - small emission testing lib for flows (hot or cold)
    const val TURBINE = "app.cash.turbine:turbine:0.8.0"
}

fun DependencyHandler.kotlinDependencies() {
    implementation(Libraries.KOTLIN_STDLIB_JDK7)
    implementation(Libraries.KOTLIN_REFLECT)
}

fun DependencyHandler.coroutineDependencies() {
    implementation(Libraries.KOTLINX_COROUTINES_CORE)
    implementation(Libraries.KOTLINX_COROUTINES_ANDROID)
}

fun DependencyHandler.koinDependencies() {
    implementation(Libraries.KOIN_KOTLIN)
}

fun DependencyHandler.koinAndroidDependencies() {
    implementation(Libraries.KOIN_ANDROID)
}

fun DependencyHandler.coreLibraryDesugaringDependencies() {
    coreLibraryDesugaring(Libraries.CORE_LIBRARY_DESUGARING)
}

fun DependencyHandler.composeDependencies() {
    implementation(Libraries.COMPOSE_COMPILER)
    implementation(Libraries.COMPOSE_UI)
    implementation(Libraries.COMPOSE_UI_TOOLING_PREVIEW)
    implementation(Libraries.COMPOSE_FOUNDATION)
    implementation(Libraries.COMPOSE_ANIMATION)
    implementation(Libraries.COMPOSE_MATERIAL)
    implementation(Libraries.COMPOSE_MATERIAL_ICONS_CORE)
    implementation(Libraries.COMPOSE_MATERIAL_ICONS_EXTENDED)
    implementation(Libraries.COMPOSE_MATERIAL3_WINDOW_SIZE)
    implementation(Libraries.COMPOSE_LIVE_DATA)
    debugImplementation(Libraries.COMPOSE_UI_TOOLING)
}

fun DependencyHandler.accompanistDependencies() {
    implementation(Libraries.ACCOMPANIST_WEBVIEW)
    implementation(Libraries.ACCOMPANIST_NAVIGATION_ANIMATION)
    implementation(Libraries.ACCOMPANIST_PERMISSIONS)
}

fun DependencyHandler.launchPadDependencies() {
    implementation(Libraries.LAUNCHPAD_COMPOSE)
}

//fun DependencyHandler.coilDependencies() {
//    implementation(Libraries.COIL)
//    implementation(Libraries.COIL_COMPOSE_EXT)
//}

fun DependencyHandler.appCompatDependencies() {
    implementation(Libraries.APP_COMPAT)
}

fun DependencyHandler.activityDependencies() {
    implementation(Libraries.COMPOSE_ACTIVITY)
}

fun DependencyHandler.androidxStartupDependencies() {
    implementation(Libraries.STARTUP)
}

fun DependencyHandler.lifecycleDependencies() {
    implementation(Libraries.LIFECYCLE_LIVEDATA_KTX)
    implementation(Libraries.LIFECYCLE_COMPOSE)
}

fun DependencyHandler.navigationDependencies() {
    implementation(Libraries.NAVIGATION_COMPOSE)
}

fun DependencyHandler.materialDependencies() {
    implementation(Libraries.MATERIAL)
    implementation(Libraries.MATERIAL_COMPOSE_THEME_ADAPTER)
}

fun DependencyHandler.coreKtxDependencies() {
    implementation(Libraries.CORE_KTX)
}

fun DependencyHandler.commonsCodecDependencies() {
    implementation(Libraries.COMMONS_CODEC)
}


fun DependencyHandler.chuckerDependencies(devConfigurations: List<Configuration>, productionConfiguration: Configuration) {
    // Only add dependency for dev configurations in the list
    devConfigurations.forEach { devConfiguration: Configuration ->
        add(devConfiguration.name, Libraries.CHUCKER)
    }
    // Production configuration is a no-op
    add(productionConfiguration.name, Libraries.CHUCKER_NO_OP) // note the releaseImplementation no-op
}


// Test specific dependency groups
fun DependencyHandler.junitDependencies() {
    testImplementation(TestLibraries.JUNIT)
}

fun DependencyHandler.mockitoKotlinDependencies() {
    testImplementation(TestLibraries.MOCKITO_KOTLIN)
}

fun DependencyHandler.truthDependencies() {
    testImplementation(TestLibraries.TRUTH)
}

fun DependencyHandler.archCoreTestingDependencies() {
    testImplementation(TestLibraries.ARCH_CORE_TESTING)
}


fun DependencyHandler.androidxCoreDependencies() {
    androidTestImplementation(TestLibraries.ANDROIDX_TEST_CORE)
    androidTestImplementation(TestLibraries.ANDROIDX_TEST_CORE_KTX)
}

fun DependencyHandler.kotlinxCoroutineTestingDependencies() {
    testImplementation(TestLibraries.KOTLINX_COROUTINE_TESTING)
}

fun DependencyHandler.turbineDependencies() {
    testImplementation(TestLibraries.TURBINE)
}
