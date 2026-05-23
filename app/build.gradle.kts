import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

// Cargar variables desde local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.example.logisticareparto"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.logisticareparto"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Inyectar las variables en BuildConfig
        // Usamos una lógica para asegurar que el valor tenga comillas exactas al generarse en Java
        fun formatProperty(key: String): String {
            val value = localProperties.getProperty(key) ?: ""
            // Si el valor ya viene con comillas desde local.properties, no agregamos más
            return if (value.startsWith("\"") && value.endsWith("\"")) value else "\"$value\""
        }

        buildConfigField("String", "FIRESTORE_PROJECT_ID", formatProperty("firestore.projectId"))
        buildConfigField("String", "FIRESTORE_API_KEY", formatProperty("firestore.apiKey"))
        buildConfigField("String", "FIRESTORE_APP_ID", formatProperty("firestore.applicationId"))
        
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", formatProperty("cloudinary.cloudName"))
        buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", formatProperty("cloudinary.uploadPreset"))
        
        // Extraer la API Key de Maps sin comillas para el manifiesto
        val rawMapsKey = localProperties.getProperty("maps.apiKey") ?: ""
        manifestPlaceholders["MAPS_API_KEY"] = rawMapsKey.replace("\"", "")
        
        buildFeatures { 
            buildConfig = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("androidx.navigation:navigation-compose:2.9.8")
    
    // google Maps para Compose
    implementation("com.google.maps.android:maps-compose:4.4.1")
    implementation("com.google.android.gms:play-services-maps:19.0.0")

    // coil para cargar imágenes
    implementation("io.coil-kt:coil-compose:2.7.0")

    // cloudinary para subir imágenes
    implementation("com.cloudinary:cloudinary-android:3.1.2")
}
