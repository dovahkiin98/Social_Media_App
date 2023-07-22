// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.tools.build.gradle) apply false
    alias(libs.plugins.kotlin.gradle) apply false
    alias(libs.plugins.hilt.android.gradle) version libs.versions.hilt apply false
}
true // Needed to make the Suppress annotation work for the plugins block