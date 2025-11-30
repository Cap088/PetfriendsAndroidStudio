plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.camilop.petfriendsapp_kotlin"
    // Es recomendable usar una versión reciente estable, como 34.
    compileSdk = 36

    defaultConfig {
        applicationId = "com.camilop.petfriendsapp_kotlin"
        minSdk = 23
        targetSdk = 36
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

    // Aquí se corrige la sintaxis de buildFeatures para Kotlin
    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

// Definición de versiones para mejor mantenimiento
val nav_version = "2.7.5"
val lifecycle_version = "2.7.0"
val retrofit_version = "2.9.0"

dependencies {

    // CORE ANDROID (Usando sintaxis de Version Catalogs si están definidos)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    // Se elimina la implementación duplicada de androidx.core:core-ktx:1.12.0

    implementation("com.google.code.gson:gson:2.10.1")

    // VIEWMODEL y LIVEDATA (Arquitectura)
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")

    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")

    // Para Coroutines en LiveData
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")

    // NAVIGATION COMPONENT

    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
    // Se corrige implementación(naviga)


    // RETROFIT & GSON (Llamadas a la API de Node.js)

    // Cliente Retrofit principal
    implementation("com.squareup.retrofit2:retrofit:$retrofit_version")
    // Conversor de JSON (Se corrige la sintaxis incorrecta)
    implementation("com.squareup.retrofit2:converter-gson:$retrofit_version")
    // OkHttp logging para ver peticiones/respuestas en Logcat (opcional, pero útil)
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    // Se elimina implementation(components.google.gson:gson:2.9.0) y se usa el conversor de Retrofit


    // --------------------------------------------------------
    // CÓDIGO DE BARRAS (ZXING)
    // --------------------------------------------------------
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // LIBRERÍAS ESPECIALIZADAS

    // TOASTY (Mensajes de notificación mejorados)
    implementation("com.github.GrenderG:Toasty:1.5.2")

    // HOLOGRAPH (Gráficos)
    //implementation("com.github.DmitriPrichard:holograph:1.0.1")

    // ITEXTPDF5 (Generación de PDF)
    // Versión estable adecuada para Android
    implementation("com.itextpdf:itext7-core:7.2.5")

    // Dependencias para OkHttp (Cliente HTTP)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Dependencia para Gson (Serialización/Deserialización JSON)
    implementation("com.google.code.gson:gson:2.10.1")

    // Dependencias requeridas para Coroutines (Usadas por GlobalScope.launch en CartFragment)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Para permisos de almacenamiento
    implementation ("androidx.activity:activity-ktx:1.8.0")
    implementation ("androidx.fragment:fragment-ktx:1.6.1")

    implementation ("com.github.bumptech.glide:glide:4.16.0")

    // PRUEBAS

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}