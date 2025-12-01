// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    // Correct Kotlin DSL syntax uses id("...") with double quotes
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false

    // Added the Google Services plugin, which is required for Firebase
    id("com.google.gms.google-services") version "4.4.2" apply false
}
