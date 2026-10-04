@file:Suppress("UnstableApiUsage")

import com.android.build.api.variant.impl.VariantOutputImpl

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.app.shared)
    implementation(projects.domain)
    implementation(projects.data)
    implementation(libs.koin.core)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.ktor.client.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.core.ktx)
    implementation(libs.coil.singleton)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.hiddenapibypass)
    implementation(libs.rikka.shizuku.provider)
    implementation(libs.rikka.shizuku.api)
    implementation(libs.focus.api)
}

android {
    namespace = ProjectConfig.PACKAGE_NAME

    compileSdk {
        version = release(ProjectConfig.Android.COMPILE_SDK) {
            minorApiLevel = ProjectConfig.Android.COMPILE_SDK_MINOR
        }
    }

    defaultConfig {
        applicationId = ProjectConfig.PACKAGE_NAME
        minSdk = ProjectConfig.Android.MIN_SDK
        targetSdk = ProjectConfig.Android.TARGET_SDK
        versionCode = resolveVersionCode()
        versionName = ProjectConfig.VERSION_NAME

        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    signingConfigs {
        // Release 使用 Android 默认生成的 debug keystore。
        // 该签名仅适合测试、内部构建和 CI 验证，不适合正式发布。
        getByName("debug") {
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            vcsInfo.include = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules-android.pro",
            )

            signingConfig = signingConfigs.getByName("debug")
        }

        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    androidResources {
        localeFilters.addAll(listOf("en", "zh"))
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    packaging {
        jniLibs {
            excludes += "lib/*/libandroidx.graphics.path.so"
        }

        resources {
            excludes += arrayOf(
                "/META-INF/*",
                "/META-INF/androidx/**",
                "/META-INF/versions/**",
                "/org/bouncycastle/**",
                "/org/apache/commons/**",
                "/kotlin/**",
                "/kotlinx/**",
                "/okhttp3/**",
                "/*.txt",
                "/*.bin",
                "/*.json",
            )
        }
    }
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            (output as? VariantOutputImpl)?.outputFileName?.set(
                output.versionName.zip(output.versionCode) { versionName, versionCode ->
                    "${ProjectConfig.APP_NAME}-v${versionName}(${versionCode})${
                        if (variant.buildType == "debug") "_debug" else ""
                    }.apk"
                }
            )
        }
    }
}
