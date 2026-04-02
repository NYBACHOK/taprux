import com.android.build.gradle.tasks.MergeSourceSetFolders
import com.nishtahir.CargoBuildTask
import com.nishtahir.CargoExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.rust.android)
}

android {
  namespace = "com.ghuba.shared"
  compileSdk {
    version = release(36) { minorApiLevel = 1 }
  }

  defaultConfig {
    minSdk = 32
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }


  sourceSets {
    getByName("main") {
      java.srcDir("${projectDir}/../generated")
    }
  }
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_11)
  }
}

dependencies {
  implementation(libs.jna) {
    artifact { type = "aar" }
  }
}

extensions.configure<CargoExtension>("cargo") {
  module = "../.."
  libname = "taprux-core"
  profile = "debug"
  targets = listOf("arm64", ) // "arm", "x86", "x86_64"
  extraCargoBuildArguments = listOf("--package", "taprux-core", "--features", "uniffi")

  cargoCommand = "cargo"
  rustcCommand = "rustc"
  pythonCommand = "python3"
}

afterEvaluate {
  project.tasks.configureEach {
    if (name.startsWith("generate") && name.endsWith("Assets")) {
      dependsOn("cargoBuild")
    }
  }
  project.tasks.withType(CargoBuildTask::class.java).configureEach {
    val buildTask = this
    project.tasks.withType(MergeSourceSetFolders::class.java).configureEach {
      val folderName = buildTask.toolchain?.folder
      if (folderName != null) {
        inputs.dir(layout.buildDirectory.dir("rustJniLibs/$folderName"))
      }
      dependsOn(buildTask)
    }
  }
}

// Ensure final JNI merge tasks depend on Cargo
project.tasks.matching { it.name.matches(Regex("merge.*JniLibFolders")) }.configureEach {
  inputs.dir(layout.buildDirectory.dir("rustJniLibs/android"))
  dependsOn("cargoBuild")
}