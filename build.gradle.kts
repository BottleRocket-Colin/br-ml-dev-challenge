// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(Config.BuildScriptPlugins.ANDROID_GRADLE)
        classpath(Config.BuildScriptPlugins.KOTLIN_GRADLE)
        classpath(Config.BuildScriptPlugins.GRADLE_VERSIONS)
//        classpath ("com.google.gms:google-services:4.3.14")
//        classpath ("com.google.ar.sceneform:plugin:1.17.1")
    }
}

plugins {
    id(Config.ApplyPlugins.KT_LINT) version Config.KTLINT_GRADLE_VERSION
    id(Config.ApplyPlugins.KSP) version Config.KSP_VERSION
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven (url= "https://jitpack.io" )
    }
}

subprojects {
    // Cannot use plugins {} here so using apply (compilation error)
    apply(plugin = Config.ApplyPlugins.KT_LINT)
    apply(plugin = Config.ApplyPlugins.GRADLE_VERSIONS)

    // See README.md for more info on ktlint as well as https://github.com/JLLeitschuh/ktlint-gradle#configuration
    ktlint {
        version.set(Config.KTLINT_VERSION)
        android.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false)
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}