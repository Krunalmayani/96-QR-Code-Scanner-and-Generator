import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.oss.licenses)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

val localProps =
    Properties().apply {
        val f = rootProject.file("local.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }

/** Real ad ids come from local.properties (ads.*) or environment (QR_ADS_*); otherwise Google's test ids. */
fun adId(key: String, env: String, testId: String): String =
    (localProps.getProperty(key) ?: System.getenv(env))?.takeIf { it.isNotBlank() } ?: testId

val keystoreProps =
    Properties().apply {
        val f = rootProject.file("keystore.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }

android {
    namespace = "com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        // HiltTestRunner (androidTest) extends AndroidJUnitRunner and swaps in HiltTestApplication.
        testInstrumentationRunner = "com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.HiltTestRunner"

        // Google's public TEST ids. Replace per app (see README, "Going live").
        resValue("string", "admob_app_id", "ca-app-pub-3940256099942544~3347511713")
        buildConfigField("String", "AD_BANNER_ID", "\"ca-app-pub-3940256099942544/9214589741\"")
        buildConfigField("String", "AD_INTERSTITIAL_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
        buildConfigField("String", "AD_REWARDED_ID", "\"ca-app-pub-3940256099942544/5224354917\"")
        buildConfigField("String", "AD_APP_OPEN_ID", "\"ca-app-pub-3940256099942544/9257395921\"")
        buildConfigField("String", "AD_NATIVE_ID", "\"ca-app-pub-3940256099942544/2247696110\"")
        // Build-time kill switch for all ads (the remote flag can also disable them at runtime).
        buildConfigField("boolean", "ADS_ENABLED", "true")
    }

    signingConfigs {
        // Release signing: keystore.properties (local) or QR_KEYSTORE_* environment variables (CI). Never committed.
        val envStore = System.getenv("QR_KEYSTORE_FILE")
        if (keystoreProps.containsKey("storeFile")) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        } else if (!envStore.isNullOrBlank()) {
            create("release") {
                storeFile = file(envStore)
                storePassword = System.getenv("QR_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("QR_KEY_ALIAS")
                keyPassword = System.getenv("QR_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            // Release uses real ids when provided (see docs/ADS_SETUP.md); test ids otherwise.
            resValue("string", "admob_app_id", adId("ads.appId", "QR_ADS_APP_ID", "ca-app-pub-3940256099942544~3347511713"))
            buildConfigField(
                "String",
                "AD_BANNER_ID",
                "\"${adId("ads.bannerId", "QR_ADS_BANNER_ID", "ca-app-pub-3940256099942544/9214589741")}\"",
            )
            buildConfigField(
                "String",
                "AD_INTERSTITIAL_ID",
                "\"${adId("ads.interstitialId", "QR_ADS_INTERSTITIAL_ID", "ca-app-pub-3940256099942544/1033173712")}\"",
            )
            buildConfigField(
                "String",
                "AD_APP_OPEN_ID",
                "\"${adId("ads.appOpenId", "QR_ADS_APP_OPEN_ID", "ca-app-pub-3940256099942544/9257395921")}\"",
            )
            buildConfigField(
                "String",
                "AD_NATIVE_ID",
                "\"${adId("ads.nativeId", "QR_ADS_NATIVE_ID", "ca-app-pub-3940256099942544/2247696110")}\"",
            )
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        // java.time (parsers, history grouping) needs desugaring below API 26.
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
            // The architecture tests (src/test/.../architecture) scan sources, resources and the manifest from here.
            it.systemProperty("repo.root", rootDir.absolutePath)
        }
        unitTests.isReturnDefaultValues = true
        // Gradle Managed Devices for `./gradlew pixel2Api24DebugAndroidTest` / `pixel6Api35DebugAndroidTest` (see docs/TESTING.md).
        managedDevices {
            localDevices {
                create("pixel2Api24") {
                    device = "Pixel 2"
                    apiLevel = 24
                    systemImageSource = "aosp"
                }
                create("pixel6Api35") {
                    device = "Pixel 6"
                    apiLevel = 35
                    systemImageSource = "google"
                }
            }
        }
    }

    sourceSets["androidTest"].assets.srcDir("$projectDir/schemas")

    packaging {
        resources.excludes += setOf("/META-INF/{AL2.0,LGPL2.1}", "/META-INF/LICENSE*")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    // Advisory: reports are produced in CI but style findings do not fail the build.
    ignoreFailures = true
    config.setFrom(rootProject.file("config/detekt.yml"))
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.mlkit.barcode)
    implementation(libs.zxing.core)

    implementation(libs.timber)
    implementation(libs.play.review)
    implementation(libs.play.app.update)
    implementation(libs.play.oss.licenses)
    implementation(libs.ump)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.config)

    implementation(libs.gma.next.gen)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.room.testing)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.mockk)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.junit4)
    // Hilt end-to-end tests: HiltAndroidRule, HiltTestApplication, @TestInstallIn.
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
