import java.util.Properties
import java.io.FileInputStream

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.hilt.gradle)
    alias(libs.plugins.ksp)
}

// [1][0][0][00] ... [9+][9][9][99]
val versionMajor = 1
val versionMinor = 1
val versionPatch = 0
val versionBuild = 1 // bump for dogfood builds, public betas, etc.
android {
    signingConfigs {
        create("release") {
            val props = Properties()
            props.load(FileInputStream(rootProject.file("local.properties")))
            keyAlias = props.getProperty("keyAlias")
            keyPassword = props.getProperty("keyPassword")
            storeFile = file(props.getProperty("storeFile"))
            storePassword = props.getProperty("storePassword")
        }
    }
    compileSdk = 35
    defaultConfig {
        applicationId = "io.github.hyuwah.catatanku"
        minSdk = 21
        targetSdk = 35
        versionCode = (versionMajor * 10000) + (versionMinor * 1000) + (versionPatch * 100) + versionBuild
        versionName = "${versionMajor}.${versionMinor}.${versionPatch}"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }
    buildFeatures {
        viewBinding = true
    }
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )

            applicationVariants.all {
                val variant = this
                variant.outputs.all {
                    val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
                    output.outputFileName = "CatatanKu-release-" + defaultConfig.versionName + ".apk"
                }
            }
        }
    }
    compileOptions {
        targetCompatibility = JavaVersion.VERSION_17
        sourceCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    productFlavors {
    }
    namespace = "io.github.hyuwah.catatanku"
}

kapt {
    correctErrorTypes = true
    useBuildCache = true
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.browser)

    implementation(libs.core.ktx)
    implementation(libs.material)

    implementation(libs.bundles.lifecycle)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    implementation(libs.bundles.coroutines)

    implementation(libs.android.about.page)
    implementation(libs.lottie)
    implementation(libs.bundles.markwon) {
        exclude(group = "org.jetbrains", module = "annotations-java5")
    }
    kapt(libs.prism4j.bundler) {
        exclude(group = "org.jetbrains", module = "annotations-java5")
    }

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
