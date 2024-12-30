import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.lang.MissingPropertyException
import org.apache.tools.ant.filters.ReplaceTokens

import java.time.LocalTime
import java.util.*

plugins {
	id("com.github.johnrengelman.shadow") version "8.1.1"
	`java-library`
	`maven-publish`
	checkstyle
}

configurations {
	create("testShadow")
	create("testImplementationKotlin") {
		extendsFrom(configurations["testShadow"])
	}
}


allprojects {
	repositories {
		mavenCentral() // todo use gradle.properties
		maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
		maven("https://oss.sonatype.org/content/repositories/snapshots/")
		maven("https://repo.papermc.io/repository/maven-public/")
		maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
		maven("https://ci.emc.gs/nexus/content/groups/aikar/")
	}
}

dependencies {
	"shadow"("io.papermc:paperlib:1.0.8")
	"shadow"("org.bstats:bstats-bukkit:3.0.2")
	"shadow"("net.kyori:adventure-text-serializer-bungeecord:4.3.2")

	implementation("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
	implementation("com.google.code.findbugs:findbugs:3.0.1")
	implementation("org.joml:joml:1.10.5")
	implementation("com.sk89q.worldguard:worldguard-legacy:7.0.0-SNAPSHOT")
	implementation("net.milkbowl.vault:Vault:1.7.3") {
		exclude(group = "org.bstats", module = "bstats-bukkit")
	}

	implementation(fileTree("dir" to "lib", "include" to "*.jar"))

	"testShadow"("junit:junit:4.13.2")
	"testShadow"("org.easymock:easymock:5.4.0")
}

checkstyle {
	configFile = file("checkstyle.xml")
	sourceSets = emptyList()
}

tasks.register("checkAliases") {
	description = "Checks for the existence of aliases."
	doLast {
		val aliasFolder = project.file("skript-aliases")
		if (!aliasFolder.exists() || aliasFolder.listFiles()?.isEmpty() == true) {
			throw InvalidUserDataException("Aliases are missing from 'skript-aliases' folder. Consider fetching submodules with 'git submodule update --init'.")
		}
	}
}

tasks.register<ShadowJar>("testJar") {
	dependsOn("compileTestJava")
	archiveFileName.set("Skript-JUnit.jar")
	from(sourceSets["test"].output, sourceSets["main"].output, project.configurations["testShadow"])
}

tasks.jar {
	dependsOn("checkAliases")
	archiveFileName.set(if (project.hasProperty("jarName")) "Skript-${project.version}.jar" else project.property("jarName") as String)
	from(sourceSets["main"].output)
}

tasks.build {
	dependsOn(tasks.jar)
}

tasks.test {
	exclude("**/*")
}

tasks.register<Jar>("sourceJar") {
	from(sourceSets["main"].allJava)
	archiveClassifier.set("sources")
}

tasks.withType<ShadowJar> {
	configurations = listOf(project.configurations["shadow"])

	dependencies {
		include(dependency("io.papermc:paperlib"))
		include(dependency("org.bstats:bstats-bukkit"))
		include(dependency("org.bstats:bstats-base"))
		include(dependency("net.kyori:adventure-text-serializer-bungeecord"))
	}

	relocate("io.papermc.lib", "ch.njol.skript.paperlib")
	relocate("org.bstats", "ch.njol.skript.bstats")

	manifest {
		attributes(
			"Name" to "ch/njol/skript",
			"Automatic-Module-Name" to "ch.njol.skript",
			"Sealed" to "true"
		)
	}

	from("skript-aliases") {
		into("aliases-english")
	}
}

tasks.processResources {
	filter(
		org.apache.tools.ant.filters.ReplaceTokens::class,
		"tokens" to mapOf(
			"version" to project.property("version"),
			"today" to "unknown",
			"release-flavor" to "selfbuilt-unknown",
			"release-channel" to "none",
			"release-updater" to "ch.njol.skript.update.NoUpdateChecker",
			"release-source" to "",
			"release-download" to "null"
		)
	)
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			groupId = "com.github.SkriptLang"
			artifactId = "Skript"
			version = project.version.toString()
			artifact(tasks["sourceJar"])
			artifact(tasks["jar"])
		}
	}

	repositories {
		maven {
			name = "repo"
			url = uri("https://repo.skriptlang.org/releases")
			credentials {
				username = System.getenv("MAVEN_USERNAME")
				password = System.getenv("MAVEN_PASSWORD")
			}
		}
	}
}

tasks.register("testNaming") {
	doLast {
		project.file("src/test/skript/tests/regressions").listFiles()?.forEach { file ->
			val name = file.name
			if (name.lowercase(Locale.ENGLISH) != name) {
				throw InvalidUserDataException("Invalid test name: $name")
			}
		}
		project.file("src/test/skript/tests/syntaxes").listFiles()?.forEach { dir ->
			dir.listFiles()?.forEach { file ->
				val name = file.name
				if (!name.startsWith(".") && !Character.isJavaIdentifierStart(name.codePointAt(0))) {
					throw InvalidUserDataException("invalid test name: $name")
				}
			}
		}
	}
}

enum class Modifiers {
	DEV_MODE, GEN_NIGHTLY_DOCS, GEN_RELEASE_DOCS, DEBUG, PROFILE, JUNIT
}

fun createTestTask(name: String, desc: String, environments: String, javaVersion: Int, timeout: Long, vararg modifiers: Modifiers) {
	val actualTimeout = if (timeout == 0L) 300000L else timeout
	val junit = modifiers.contains(Modifiers.JUNIT)
	val releaseDocs = modifiers.contains(Modifiers.GEN_RELEASE_DOCS)
	val docs = modifiers.contains(Modifiers.GEN_NIGHTLY_DOCS) || releaseDocs

	val artifact = "build${File.separator}libs${File.separator}" + when {
		junit -> "Skript-JUnit.jar"
		releaseDocs -> "Skript-${project.version}.jar"
		else -> "Skript-nightly.jar"
	}

	tasks.register<JavaExec>(name) {
		description = desc

		when {
			junit -> dependsOn("testJar")
			releaseDocs -> dependsOn("githubRelease", "testNaming")
			else -> dependsOn("nightlyRelease", "testNaming")
		}

		javaLauncher.set(javaToolchains.launcherFor {
			languageVersion.set(JavaLanguageVersion.of(javaVersion))
		})

		if (modifiers.contains(Modifiers.DEV_MODE)) {
			standardInput = System.`in`
		}

		group = "execution"
		classpath = files(
			artifact,
			project.configurations["runtimeClasspath"].find { it.name.startsWith("gson") },
			sourceSets["main"].runtimeClasspath
		)
		mainClass.set("ch.njol.skript.test.platform.PlatformMain")

		args = listOf(
			"build/test_runners",
			if (junit) "src/test/skript/junit" else "src/test/skript/tests",
			"src/test/resources/runner_data",
			environments,
			modifiers.contains(Modifiers.DEV_MODE).toString(),
			docs.toString(),
			junit.toString(),
			modifiers.contains(Modifiers.DEBUG).toString(),
			project.findProperty("verbosity")?.toString() ?: "null",
			actualTimeout.toString()
		)

		doFirst {
			if (!gradle.taskGraph.hasTask(":tasks") && !gradle.startParameter.isDryRun && modifiers.contains(Modifiers.PROFILE)) {
				if (!project.hasProperty("profiler")) {
					throw MissingPropertyException("Add parameter -Pprofiler=<path to profiler>", "profiler", String::class.java)
				}
				@Suppress("UNCHECKED_CAST")
				(args as MutableList<String>).add("-agentpath:${project.property("profiler")}=port=8849,nowait")
			}
		}
	}
}

val java21 = 21
val java17 = 17
val java11 = 11

val latestEnv = "java21/paper-1.21.3.json"
val latestJava = java21
val oldestJava = java11

val latestJUnitEnv = latestEnv
val latestJUnitJava = latestJava

java {
	disableAutoTargetJvm()
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile>().configureEach {
	options.release.set(21)
	options.compilerArgs.addAll(listOf("--enable-preview"))
}

tasks.compileJava {
	options.encoding = "UTF-8"
}

tasks.compileTestJava {
	options.encoding = "UTF-8"
}

val environments = "src/test/skript/environments/"
val env = project.findProperty("testEnv")?.toString()?.let { "$it.json" } ?: latestEnv
val envJava = project.findProperty("testEnvJavaVersion")?.toString()?.toInt() ?: latestJava

createTestTask("quickTest", "Runs tests on one environment being the latest supported Java and Minecraft.", environments + latestEnv, latestJava, 0)
createTestTask("skriptTestJava21", "Runs tests on all Java 21 environments.", environments + "java21", java21, 0)
createTestTask("skriptTestJava17", "Runs tests on all Java 17 environments.", environments + "java17", java17, 0)
createTestTask("skriptTestJava11", "Runs tests on all Java 11 environments.", environments + "java11", java11, 0)
createTestTask("skriptTestDev", "Runs testing server and uses 'system.in' for command input, stop server to finish.", environments + env, envJava, 0, Modifiers.DEV_MODE, Modifiers.DEBUG)
createTestTask("skriptProfile", "Starts the testing server with JProfiler support.", environments + latestEnv, latestJava, -1, Modifiers.PROFILE)
createTestTask("genNightlyDocs", "Generates the Skript documentation website html files.", environments + env, envJava, 0, Modifiers.GEN_NIGHTLY_DOCS)
createTestTask("genReleaseDocs", "Generates the Skript documentation website html files for a release.", environments + env, envJava, 0, Modifiers.GEN_RELEASE_DOCS)

tasks.register("skriptTest") {
	description = "Runs tests on all environments."
	dependsOn("skriptTestJava11", "skriptTestJava17", "skriptTestJava21")
}

createTestTask("JUnitQuick", "Runs JUnit tests on one environment being the latest supported Java and Minecraft.", environments + latestJUnitEnv, latestJUnitJava, 0, Modifiers.JUNIT)
createTestTask("JUnitJava21", "Runs JUnit tests on all Java 21 environments.", environments + "java21", java21, 0, Modifiers.JUNIT)
createTestTask("JUnitJava17", "Runs JUnit tests on all Java 17 environments.", environments + "java17", java17, 0, Modifiers.JUNIT)
createTestTask("JUnitJava11", "Runs JUnit tests on all Java 11 environments.", environments + "java11", java11, 0, Modifiers.JUNIT)

tasks.register("JUnit") {
	description = "Runs JUnit tests on all environments."
	dependsOn("JUnitJava11", "JUnitJava17", "JUnitJava21")
}

tasks.register<ProcessResources>("githubResources") {
	from("src/main/resources") {
		include("**")
		val version = project.property("version").toString()
		val channel = if (version.contains("-")) "prerelease" else "stable"
		filter(ReplaceTokens::class, "tokens" to mapOf(
			"version" to version,
			"today" to LocalTime.now().toString(),
			"release-flavor" to "skriptlang-github",
			"release-channel" to channel,
			"release-updater" to "ch.njol.skript.update.GithubChecker",
			"release-source" to "https://api.github.com/repos/SkriptLang/Skript/releases",
			"release-download" to "null"
		))
	}
	into("build/resources/main")
}

tasks.register<ShadowJar>("githubRelease") {
	from(sourceSets["main"].output)
	dependsOn("githubResources")
	archiveFileName.set("Skript-${project.version}.jar")
	manifest {
		attributes(mapOf(
			"Name" to "ch/njol/skript",
			"Automatic-Module-Name" to "ch.njol.skript",
			"Sealed" to "true"
		))
	}
}
tasks.register<ProcessResources>("spigotResources") {
	from("src/main/resources") {
		include("**")
		val version = project.property("version").toString()
		val channel = if (version.contains("-")) "prerelease" else "stable"
		filter(ReplaceTokens::class, "tokens" to mapOf(
			"version" to version,
			"today" to LocalTime.now().toString(),
			"release-flavor" to "skriptlang-spigot",
			"release-channel" to channel,
			"release-updater" to "ch.njol.skript.update.GithubChecker",
			"release-source" to "https://api.github.com/repos/SkriptLang/Skript/releases",
			"release-download" to "https://www.spigotmc.org/resources/skript.114544/"
		))
	}
	into("build/resources/main")
}

tasks.register<ShadowJar>("spigotRelease") {
	from(sourceSets["main"].output)
	dependsOn("spigotResources")
	archiveFileName.set("Skript-spigot.jar")
	manifest {
		attributes(mapOf(
			"Name" to "ch/njol/skript",
			"Automatic-Module-Name" to "ch.njol.skript",
			"Sealed" to "true"
		))
	}
}

tasks.register<ProcessResources>("nightlyResources") {
	from("src/main/resources") {
		include("**")
		val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
			.redirectErrorStream(true)
			.start()
		val hash = process.inputStream.bufferedReader().use { it.readText().trim() }
		val version = "${project.property("version")}-nightly-$hash"
		filter(ReplaceTokens::class, "tokens" to mapOf(
			"version" to version,
			"today" to LocalTime.now().toString(),
			"release-flavor" to "skriptlang-nightly",
			"release-channel" to "prerelease",
			"release-updater" to "ch.njol.skript.update.NoUpdateChecker",
			"release-source" to "",
			"release-download" to "null"
		))
	}
	into("build/resources/main")
}

tasks.register<ShadowJar>("nightlyRelease") {
	from(sourceSets["main"].output)
	dependsOn("nightlyResources")
	archiveFileName.set("Skript-nightly.jar")
	manifest {
		attributes(mapOf(
			"Name" to "ch/njol/skript",
			"Automatic-Module-Name" to "ch.njol.skript",
			"Sealed" to "true"
		))
	}
}

tasks.javadoc {
	mustRunAfter(tasks.withType<ProcessResources>())
	title = "Skript ${project.property("version")}"
	source = sourceSets["main"].allJava
	setExcludes(listOf(
		"ch/njol/skript/conditions/**",
		"ch/njol/skript/expressions/**",
		"ch/njol/skript/effects/**",
		"ch/njol/skript/events/**",
		"ch/njol/skript/sections/**",
		"ch/njol/skript/structures/**",
		"ch/njol/skript/structures/**",
		"ch/njol/skript/lang/function/EffFunctionCall.java",
		"ch/njol/skript/lang/function/ExprFunctionCall.java",
		"ch/njol/skript/hooks/**",
		"ch/njol/skript/test/**"
	))
	classpath = configurations["compileClasspath"] + sourceSets["main"].output
	(options as StandardJavadocDocletOptions).apply {
		encoding = "UTF-8"
		addStringOption("Xdoclint:none", "-quiet")
	}
}