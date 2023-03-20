plugins {
    id("com.android.application") version libs.versions.gradle apply false
    id ("com.android.library") version libs.versions.gradle apply false
    id ("org.jetbrains.kotlin.android") version libs.versions.kotlin apply false
    id("com.google.dagger.hilt.android") version libs.versions.hilt apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}