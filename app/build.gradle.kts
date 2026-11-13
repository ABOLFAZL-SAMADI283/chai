plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "ir.chai.focus"
    compileSdk = 35

    defaultConfig {
        applicationId = "ir.chai.focus"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"
        resourceConfigurations += listOf("fa", "en")

        // TODO(ads): وقتی شبکهٔ تبلیغ انتخاب شد، این placeholderها و dependency تپسل فعال می‌شوند.
        // addManifestPlaceholders(mapOf("TapsellMediationAppKey" to "YOUR_APP_ID"))
    }

    signingConfigs {
        // برای ساخت CI، کلید از secrets گیرفته می‌شود
        create("release") {
            // مسیر نسبت به ریشهٔ پروژه (keystore در CI ریشهٔ ریپو ساخته می‌شود)
            storeFile = rootProject.file(System.getenv("KEYSTORE_PATH") ?: "release.jks")
            storePassword = System.getenv("KEYSTORE_PASS")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASS")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.02"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.core:core-ktx:1.13.1")

    // TODO(ads): SDK تپسل فعلاً فعال نیست (بودجه/شبکه انتخاب نشده)
    // implementation("ir.tapsell:tapsell:1.0.2-beta08")
    // implementation("ir.tapsell.mediation.adapter:legacy:1.0.2-beta08")
}
