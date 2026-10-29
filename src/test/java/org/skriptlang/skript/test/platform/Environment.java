package org.skriptlang.skript.test.platform;

import ch.njol.skript.test.utils.TestResults;
import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Test environment information.
 */
public class Environment {

	/**
	 * Name of this environment.
	 * For example, {@code paper-26.3}.
	 */
	private final String name;

	/**
	 * Resources that need to be obtained for this environment.
	 */
	private final List<Resource> resources;

	/**
	 * Where Skript should be placed under platform root.
	 * Directories created as needed.
	 */
	private final String skriptTarget;

	/**
	 * Added after platform's own JVM flags.
	 */
	private final String[] commandLine;

	public Environment(String name, List<Resource> resources, String skriptTarget, String... commandLine) {
		this.name = name;
		this.resources = resources;
		this.skriptTarget = skriptTarget;
		this.commandLine = commandLine;
	}

	public String getName() {
		return name;
	}

	public void initialize(Path dataRoot, Path runnerRoot, boolean remake) throws IOException {
		Path env = runnerRoot.resolve(name);
		boolean onlyCopySkript = Files.exists(env) && !remake;

		// Copy Skript to platform
		Path skript = env.resolve(skriptTarget);
		Files.createDirectories(skript.getParent());
		try {
			Files.copy(new File(getClass().getProtectionDomain().getCodeSource().getLocation()
				.toURI()).toPath(), skript, StandardCopyOption.REPLACE_EXISTING);
		} catch (URISyntaxException e) {
			throw new AssertionError(e);
		}

		if (onlyCopySkript) {
			return;
		}

		// Copy resources
		for (Resource resource : resources) {
			System.out.println("Fetching resource: " + resource.target());
			resource.fetch(dataRoot, env);
		}
	}

	@Nullable
	public TestResults runTests(Path runnerRoot, Path testsRoot, boolean devMode, boolean genDocs, boolean jUnit, boolean debug,
	                            String verbosity, long timeout, Set<String> jvmArgs) throws IOException, InterruptedException {

		Path env = runnerRoot.resolve(name);
		Path resultsPath = env.resolve("test_results.json");
		Files.deleteIfExists(resultsPath);
		List<String> args = new ArrayList<>();
		args.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
		args.add("-ea");
		args.add("-Dskript.testing.enabled=true");
		args.add("-Dskript.testing.dir=" + testsRoot);
		args.add("-Dskript.testing.devMode=" + devMode);
		args.add("-Dskript.testing.genDocs=" + genDocs);
		args.add("-Dskript.testing.junit=" + jUnit);
		if (!verbosity.equalsIgnoreCase("null"))
			args.add("-Dskript.testing.verbosity=" + verbosity);
		if (genDocs)
			args.add("-Dskript.forceregisterhooks=true");
		args.add("-Dskript.testing.results=test_results.json");
		args.add("-Ddisable.watchdog=true");
		if (debug)
			args.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000");
		args.add("-Duser.language=en");
		args.add("-Duser.country=US");
		args.addAll(jvmArgs);
		args.addAll(Arrays.asList(commandLine));

		Process process = new ProcessBuilder(args)
			.directory(env.toFile())
			.redirectOutput(Redirect.INHERIT)
			.redirectError(Redirect.INHERIT)
			.redirectInput(Redirect.INHERIT)
			.start();

		// When we exit, try to make them exit too
		Runtime.getRuntime().addShutdownHook(new Thread(process::destroy));

		// Catch tests running for abnormally long time
		if (!devMode && timeout > 0) {
			new Timer("runner watchdog", true).schedule(new TimerTask() {
				@Override
				public void run() {
					if (process.isAlive()) {
						System.err.println("Test environment is taking too long, failing...");
						System.exit(1);
					}
				}
			}, timeout);
		}

		int code = process.waitFor();
		if (code != 0)
			throw new IOException("environment returned with code " + code);

		// Read test results
		if (!Files.exists(resultsPath))
			return null;
		TestResults results = new Gson().fromJson(new String(Files.readAllBytes(resultsPath)), TestResults.class);
		assert results != null;
		return results;
	}

}
