import com.android.build.gradle.tasks.MergeSourceSetFolders
import com.nishtahir.CargoBuildTask
import com.nishtahir.CargoExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.rust.android)
}

android {
  namespace = "com.ghuba.taprux"
  compileSdk = 36
  ndkVersion = "29.0.14206865"

  defaultConfig { minSdk = 32 }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }

  sourceSets {
    getByName("main") {
      // types generated in kotlin
      kotlin.srcDirs("${projectDir}/../generated")
    }
  }


}

dependencies { implementation(libs.jna) { artifact { type = "aar" } } }


android.libraryVariants.configureEach {
  val profile = if (buildType.name == "release") "release" else "debug"

  extensions.configure<CargoExtension>("cargo") {
    module = "../.."
    libname = "taprux_core"
    this.profile = profile
    targets = listOf( "arm64", "arm", "x86", "x86_64")
    extraCargoBuildArguments = listOf("--package", "taprux-core", "--features", "uniffi")

    cargoCommand = System.getProperty("user.home") + "/.cargo/bin/cargo"
    rustcCommand = System.getProperty("user.home") + "/.cargo/bin/rustc"
    pythonCommand = "python3"
  }
}

afterEvaluate {
  // The `cargoBuild` task isn't available until after evaluation.
  android.libraryVariants.configureEach {
    var productFlavor = ""
    productFlavors.forEach { flavor ->
      productFlavor += flavor.name.replaceFirstChar { char -> char.uppercaseChar() }
    }
    val buildType = buildType.name.replaceFirstChar { char -> char.uppercaseChar() }

    tasks.named("generate${productFlavor}${buildType}Assets") {
      dependsOn(tasks.named("cargoBuild"))
    }

    // The below dependsOn is needed till https://github.com/mozilla/rust-android-gradle/issues/85
    // is resolved this fix was got from #118
    tasks.withType<CargoBuildTask>().forEach { buildTask ->
      tasks.withType<MergeSourceSetFolders>().configureEach {
        inputs.dir(
            File(
                File(layout.buildDirectory.asFile.get(), "rustJniLibs"),
                buildTask.toolchain?.folder!!,
            )
        )
        dependsOn(buildTask)
      }
    }
  }
}

// Ensure final JNI merge tasks depend on Cargo
project.tasks
    .matching { it.name.matches(Regex("merge.*JniLibFolders")) }
    .configureEach {
      inputs.dir(layout.buildDirectory.dir("rustJniLibs/android"))
      dependsOn("cargoBuild")
    }
