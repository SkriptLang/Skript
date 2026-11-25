import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.tools.ant.filters.ReplaceTokens

import java.time.LocalTime

plugins {
	id("java")
	id("checkstyle")
	id("maven-publish")
	id("com.gradleup.shadow") version "9.6.1"
}

repositories {
	mavenCentral()
	maven("https://repo.papermc.io/repository/maven-public/") // for Paper
	maven("https://maven.enginehub.org/repo/") // for WorldGuard
	maven("https://jitpack.io") // for Vault
}

val testShadow = configurations.create("testShadow") {
	isCanBeResolved = false
	isCanBeConsumed = false
}
configurations {
	testImplementation.get().extendsFrom(testShadow)
}

dependencies {
	// base dependencies
	implementation("com.google.code.findbugs:findbugs:3.0.1")

	// Bukkit dependencies
	implementation("io.papermc.paper:paper-api:26.3.build.+")
	shadow("org.bstats:bstats-bukkit:3.2.1")
	// bundled with Minecraft 1.19.4+ for display entity transforms
	implementation("org.joml:joml:1.10.9")
	// Plugin hook libraries
	implementation("com.sk89q.worldguard:worldguard-legacy:7.0.0-SNAPSHOT") {
		exclude(group = "org.bukkit", module = "bukkit")
		exclude(group = "org.bstats", module = "bstats-bukkit")
	}
	implementation("com.github.MilkBowl:VaultAPI:1.7.1") {
		exclude(group = "org.bukkit", module = "bukkit")
		exclude(group = "org.bstats", module = "bstats-bukkit")
	}
	implementation(fileTree("lib") { include("*.jar") })

	testShadow("junit:junit:4.13.2")
	testShadow("org.easymock:easymock:5.7.0")
}

checkstyle {
	configFile = file("checkstyle.xml")
	sourceSets = emptyList() // disables checkstyle after build task
}

/*
 * Building
 */

tasks.register("checkAliases") {
	description = "Checks for the existence of the aliases."
	val aliasFolder = file("skript-aliases")
	doLast {
		if (!aliasFolder.exists() || aliasFolder.listFiles().isNullOrEmpty()) {
			throw InvalidUserDataException("Aliases are missing from 'skript-aliases' folder." +
				" Consider fetching submodules with 'git submodule update --init'.")
		}
	}
}

tasks.replace("build", ShadowJar::class.java).apply {
	from(sourceSets.main.get().output)
	archiveFileName = "Skript-${project.version}.jar"
}

tasks.replace("jar", ShadowJar::class.java).apply {
	dependsOn(tasks.named("checkAliases"))
	from(sourceSets.main.get().output)
	archiveFileName = "Skript-${project.version}.jar"
}

tasks.register<ShadowJar>("testJar") {
	description = "Creates a JAR with test dependencies included."
	dependsOn(tasks.compileTestJava)
	archiveFileName = "Skript-JUnit.jar"
	from(sourceSets.main.get().output, sourceSets.test.get().output)
	from(provider {
		testShadow.map { if (it.isDirectory) it else zipTree(it) }
	})
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.test {
	enabled = false
}

tasks.withType<ShadowJar>().configureEach {
	configurations = listOf(project.configurations["shadow"])
	dependencies {
		include(dependency("org.bstats:bstats-base"))
		include(dependency("org.bstats:bstats-bukkit"))
	}
	relocate("org.bstats", "org.skriptlang.skript.bukkit.bstats")
	manifest {
		attributes(
			"Automatic-Module-Name" to "org.skriptlang.skript",
			"Sealed" to "true",
		)
	}
	from("skript-aliases") {
		into("aliases-english") // Change this if we get aliases in other languages
	}
}

val java21 = 21
val java25 = 25
val latestJava = java25
val oldestJava = java21

java {
	toolchain.languageVersion = JavaLanguageVersion.of(latestJava)
	// the latest versions of Paper require Java 25, but we still support Paper versions requiring Java 21
	// we do this to prevent Gradle from rejecting the Paper dependency
	disableAutoTargetJvm()
}

tasks.withType<JavaCompile>().configureEach {
	options.release = oldestJava
	options.encoding = "UTF-8"
}

/*
 * Publishing
 */

group = "org.skriptlang.skript"
version = project.property("version") as String

tasks.register<ShadowJar>("sourcesJar") {
	description = "Creates a JAR with all sources."
	from(sourceSets.main.get().allJava)
	archiveClassifier = "sources"
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			artifact(tasks.named("sourcesJar"))
			artifact(tasks.named("jar"))
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

tasks.javadoc {
	mustRunAfter(tasks.withType<ProcessResources>())
	title = "Skript ${project.property("version")}"
	source = sourceSets.main.get().allJava

	exclude(
		"ch/njol/skript/conditions/**",
		"ch/njol/skript/expressions/**",
		"ch/njol/skript/effects/**",
		"ch/njol/skript/events/**",
		"ch/njol/skript/sections/**",
		"ch/njol/skript/structures/**",
		"ch/njol/skript/lang/function/EffFunctionCall.java",
		"ch/njol/skript/lang/function/ExprFunctionCall.java",
		"ch/njol/skript/hooks/**",
		"ch/njol/skript/test/**",
	)

	classpath = configurations.compileClasspath.get() + sourceSets.main.get().output
	options.encoding = "UTF-8"
	// currently our javadoc has a lot of errors, so we need to suppress the linter
	(options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

/*
 * Releasing
 */

fun resourceTokens(
	version: String,
	today: String,
	flavor: String,
	channel: String,
	updater: String = "ch.njol.skript.update.NoUpdateChecker", // no update checking by default
	source: String = "",
	download: String = "null",
) = mapOf(
	"version" to version,
	"today" to today,
	"release-flavor" to flavor,
	"release-channel" to channel,
	"release-updater" to updater,
	"release-source" to source,
	"release-download" to download,
)

// by default, release manifest (if not using a release task) has no information
tasks.processResources {
	inputs.property("version", project.version)
	filter<ReplaceTokens>(
		"tokens" to resourceTokens(
			version = project.property("version") as String,
			today = "unknown", // avoid leaking information about system this was built on
			flavor = "selfbuilt-unknown", // note: 'selfbuilt' prefix makes updater report a custom build
			channel = "none",
		)
	)
}

tasks.register<ShadowJar>("nightlyRelease") {
	description = "JAR task for nightly releases."

	from(
		sourceSets.main.get().output.classesDirs, // use classesDirs to exclude resources, processed below instead
		sourceSets.test.get().output,
	)

	val hash = providers.exec {
		commandLine("git", "rev-parse", "--short", "HEAD")
	}.standardOutput.asText.get().trim()
	val nightlyVersion = "${project.version}-nightly-$hash"
	inputs.property("version", nightlyVersion)
	from("src/main/resources") {
		filter<ReplaceTokens>(
			"tokens" to resourceTokens(
				version = nightlyVersion,
				today = LocalTime.now().toString(),
				flavor = "skriptlang-nightly",
				channel = "prerelease",
			)
		)
	}

	archiveFileName = "Skript-nightly.jar"
}

tasks.register<ShadowJar>("githubRelease") {
	description = "JAR task for GitHub releases."

	from(sourceSets.main.get().output.classesDirs) // use classesDirs to exclude resources, processed below instead

	val releaseVersion = project.version.toString()
	val channel = if ("-" in releaseVersion) "prerelease" else "stable"
	inputs.property("version", releaseVersion)
	from("src/main/resources") {
		filter<ReplaceTokens>(
			"tokens" to resourceTokens(
				version = releaseVersion,
				today = LocalTime.now().toString(),
				flavor = "skriptlang-github",
				channel = channel,
				updater = "ch.njol.skript.update.GithubChecker",
				source = "https://api.github.com/repos/SkriptLang/Skript/releases",
				// Update checker will figure out download
			)
		)
	}

	archiveFileName = "Skript-${project.version}.jar"
}

/*
 * Testing
 */

/**
 * What a test task runs, which determines the JAR it uses and the flags passed to PlatformMain.
 */
enum class TestingRun(val testsDir: String, val docs: Boolean, val junit: Boolean) {
	TESTS("src/test/skript/tests", docs = false, junit = false),
	JUNIT("src/test/skript/junit", docs = false, junit = true),
	NIGHTLY_DOCS("src/test/skript/tests", docs = true, junit = false),
	RELEASE_DOCS("src/test/skript/tests", docs = true, junit = false),
}

val defaultTestTimeout = 300_000L // 5 minutes
val noTimeout = -1L

val environments = "src/test/skript/environments/"
val defaultEnv = "${environments}${project.property("testEnv")}.json"
val defaultEnvJava = (project.property("testEnvJavaVersion") as String).toInt()

fun registerSkriptTestTask(
	name: String,
	description: String,
	environments: String = defaultEnv,
	javaVersion: Int = defaultEnvJava,
	run: TestingRun = TestingRun.TESTS,
	timeout: Long = defaultTestTimeout,
	devMode: Boolean = false,
	debug: Boolean = false,
	profile: Boolean = false,
) = tasks.register<JavaExec>(name) {
	setDescription(description)
	group = "execution"

	val jar = when (run) {
		TestingRun.TESTS, TestingRun.NIGHTLY_DOCS -> tasks.named<ShadowJar>("nightlyRelease")
		TestingRun.JUNIT -> tasks.named<ShadowJar>("testJar")
		TestingRun.RELEASE_DOCS -> tasks.named<ShadowJar>("githubRelease")
	}

	javaLauncher = javaToolchains.launcherFor {
		languageVersion = JavaLanguageVersion.of(javaVersion)
	}
	if (devMode) {
		standardInput = System.`in`
	}

	classpath = files(
		// jar must stay first: PlatformMain installs the jar that org.skriptlang.skript.Skript was loaded from
		jar.flatMap { it.archiveFile },
		configurations.runtimeClasspath.get().filter { it.name.startsWith("gson") },
		sourceSets.main.get().runtimeClasspath,
	)
	if (run == TestingRun.RELEASE_DOCS) {
		// PlatformMain lives in the test source set, so a release jar wouldn't contain it
		classpath += sourceSets.test.get().output
	}
	mainClass = "org.skriptlang.skript.test.platform.PlatformMain"
	args(
		"build/test_runners",
		run.testsDir,
		"src/test/resources/runner_data",
		environments,
		devMode,
		run.docs,
		run.junit,
		debug,
		findProperty("verbosity") ?: "null",
		timeout,
	)

	if (profile) {
		val profiler = providers.gradleProperty("profiler")
		doFirst {
			if (!profiler.isPresent) {
				throw InvalidUserDataException("Add parameter -Pprofiler=<path to profiler>")
			}
			args("-agentpath:${profiler.get()}=port=8849,nowait")
		}
	}
}

registerSkriptTestTask(
	name = "quickTest",
	description = "Runs tests on one environment being the latest supported Java and Minecraft.",
)
val skriptTestJava21 = registerSkriptTestTask(
	name = "skriptTestJava21",
	description = "Runs tests on all Java 21 environments.",
	environments = environments + "java21",
	javaVersion = java21
)
val skriptTestJava25 = registerSkriptTestTask(
	name = "skriptTestJava25",
	description = "Runs tests on all Java 25 environments.",
	environments = environments + "java25",
	javaVersion = java25
)
registerSkriptTestTask(
	name = "skriptTestDev",
	description = "Runs testing server and uses 'system.in' for command input, stop server to finish.",
	devMode = true,
	debug = true
)
registerSkriptTestTask(
	name = "skriptProfile",
	description = "Starts the testing server with JProfiler support.",
	timeout = noTimeout,
	profile = true
)
registerSkriptTestTask(
	name = "genNightlyDocs",
	description = "Generates the Skript documentation website html files.",
	run = TestingRun.NIGHTLY_DOCS
)
registerSkriptTestTask(
	name = "genReleaseDocs",
	description = "Generates the Skript documentation website html files for a release.",
	run = TestingRun.RELEASE_DOCS
)
tasks.register("skriptTest") {
	description = "Runs tests on all environments."
	group = "execution"
	dependsOn(skriptTestJava21, skriptTestJava25)
}

registerSkriptTestTask(
	name = "JUnitQuick",
	description = "Runs JUnit tests on one environment being the latest supported Java and Minecraft.",
	run = TestingRun.JUNIT
)
val jUnitJava21 = registerSkriptTestTask(
	name = "JUnitJava21",
	description = "Runs JUnit tests on all Java 21 environments.",
	environments = environments + "java21",
	javaVersion = java21,
	run = TestingRun.JUNIT
)
val jUnitJava25 = registerSkriptTestTask(
	name = "JUnitJava25",
	description = "Runs JUnit tests on all Java 25 environments.",
	environments = environments + "java25",
	javaVersion = java25,
	run = TestingRun.JUNIT
)
tasks.register("JUnit") {
	description = "Runs JUnit tests on all environments."
	group = "execution"
	dependsOn(jUnitJava21, jUnitJava25)
}
