import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.library)
}

configure<LibraryExtension> {

  namespace = "com.ghuba.taprux.core"

  compileSdk = 36

  defaultConfig {
    minSdk = 32
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  sourceSets {
    getByName("main") {
      kotlin.directories.add("${projectDir}/../generated")
      jniLibs.directories.add("${projectDir}/../generated/jniLibs")
    }
  }
}