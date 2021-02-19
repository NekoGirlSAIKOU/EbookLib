import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
}

group = "pers.nekogirlsaikou.ebooklib"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-junit"))
    // jsoup HTML parser library @ https://jsoup.org/
    implementation("org.jsoup:jsoup:1.13.1")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.jar{
    manifest{
        attributes(
            "Created-By" to "NekoGirlSAIKOU",
            "Automatic-Module-Name" to "pers.nekogirlsaikou.ebooklib",
            "Bundle-Description" to "ebooklib is a kotlin library to read and write epub files.",
            "Bundle-Name" to "ebooklib",
            "Bundle-SymbolicName" to "pers.nekogirlsaikou.ebooklib",
            "Bundle-Vendor" to "NekoGirlSAIKOU",
            "Bundle-Version" to archiveVersion,
            "Export-Package" to "pers.nekogirlsaikou.ebooklib",
            "Import-Package" to "org.jsoup"
        )
    }
    from ("src/main") {
        include("META-INF/LICENSE")
    }
}