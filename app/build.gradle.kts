plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// versionCode עולה אוטומטית לפי מספר ההרצה ב-CI (GITHUB_RUN_NUMBER), כך שכל בנייה
// חדשה נחשבת "חדשה יותר" מהקודמת ואפשר לעדכן התקנה קיימת. בבנייה מקומית נשאר 1.
val ciVersionCode = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toIntOrNull() ?: 1

android {
    namespace = "org.tekhelet.knotadvisor"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.tekhelet.knotadvisor"
        minSdk = 26
        targetSdk = 34
        versionCode = ciVersionCode
        versionName = "0.2.0.$ciVersionCode"
    }

    // מפתח חתימה קבוע שנשמר בריפו. בלי זה, כל הרצת CI מייצרת debug.keystore חדש,
    // החתימה משתנה בין בנייה לבנייה, ואנדרואיד מסרב לעדכן התקנה קיימת ודורש
    // הסרה והתקנה מחדש. עם מפתח קבוע - ההתקנה החדשה פשוט דורסת את הישנה.
    // ראו DESIGN.md, "חתימת ה-APK ועדכון במקום".
    signingConfigs {
        create("stable") {
            storeFile = rootProject.file("keystore/tekhelet-dev.jks")
            storePassword = "tekhelet"
            keyAlias = "tekhelet-dev"
            keyPassword = "tekhelet"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("stable")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("stable")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // טעינת תמונות קשירה מ-assets/images/<method-id>/ (ראו AssetImages.kt + content/README.md)
    implementation("io.coil-kt:coil-compose:2.6.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}

// מסנכרן תוכן שמנוהל בתיקיית content/ בשורש הריפו (מחוץ למודול app) לתוך assets, כדי
// שאפשר יהיה להוסיף תמונות/טקסטים בלי לגעת ב-app/src/main בכלל. ראו content/README.md.
val syncContentImages by tasks.registering(Copy::class) {
    description = "מעתיק תמונות קשירה מ-content/images/<method-id>/ לתוך app/src/main/assets/images/"
    from(rootProject.layout.projectDirectory.dir("content/images"))
    into(layout.projectDirectory.dir("src/main/assets/images"))
}

val syncContentTexts by tasks.registering(Copy::class) {
    description = "מעתיק את content/texts.txt לתוך app/src/main/assets/, כדי ש-ContentRepository יוכל לפרסר אותו בזמן ריצה"
    from(rootProject.layout.projectDirectory.file("content/texts.txt"))
    into(layout.projectDirectory.dir("src/main/assets"))
}

tasks.named("preBuild") {
    dependsOn(syncContentImages, syncContentTexts)
}
