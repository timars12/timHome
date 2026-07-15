plugins {
    id("timHome.android.library")
    id("timHome.quality.convention.plugin")
}

android {
    namespace = "com.timhome.core.datastore"
}

dependencies {
    // Exposed: DataStoreManager's public API leaks androidx.datastore
    // Preferences types (edit {} return values), so consumers need them.
    api(libs.datastore)
}
