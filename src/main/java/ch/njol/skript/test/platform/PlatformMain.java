package ch.njol.skript.test.platform;

import ch.njol.skript.test.utils.TestResults;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main entry point of test platform. It allows running this Skript on
 * multiple testing environments.
 */
public class PlatformMain {
	
	public static void main(String... args) throws IOException, InterruptedException {
		System.out.println("Initializing Skript test platform...");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		Path runnerRoot = Paths.get(args[0]);
		Path testsRoot = Paths.get(args[1]).toAbsolutePath();
		Path dataRoot = Paths.get(args[2]);
		// allow multiple environments separated by commas
		List<Path> envPaths = new ArrayList<>();
		String envsArg = args[3];
		envsArg = envsArg.trim();
		String[] envPathStrings = envsArg.split(",");
		for (String envPath : envPathStrings) {
			envPaths.add(Paths.get(envPath.trim()));
		}
		boolean devMode = "true".equals(args[4]);
		boolean genDocs = "true".equals(args[5]);
		boolean jUnit = "true".equals(args[6]);
		boolean debug = "true".equals(args[7]);
		String verbosity = args[8].toUpperCase(Locale.ENGLISH);
		long timeout = Long.parseLong(args[9]);
		if (timeout < 0)
			timeout = 0;
		Set<String> jvmArgs = Sets.newHashSet(Arrays.copyOfRange(args, 10, args.length));
		if (jvmArgs.stream().noneMatch(arg -> arg.contains("-Xmx")))
			jvmArgs.add("-Xmx5G");

		// Load environments
		List<Environment> envs = new ArrayList<>();
		for (Path envPath : envPaths) {
			if (Files.isDirectory(envPath)) {
				envs.addAll(Files.walk(envPath).filter(path -> !Files.isDirectory(path))
					.map(path -> {
						try {
							return gson.fromJson(Files.readString(path), Environment.class);
						} catch (JsonSyntaxException | IOException e) {
							throw new RuntimeException(e);
						}
					}).toList());
			} else {
				envs.add(gson.fromJson(Files.readString(envPath), Environment.class));
			}
		}
		System.out.println("Test environments: "
				+ envs.stream().map(Environment::getName).collect(Collectors.joining(", ")));
		
		Set<String> allTests = new HashSet<>();
		Map<String, List<TestError>> failures = new HashMap<>();
		
		boolean docsFailed = false;
		Map<Environment, TestResults> collectedResults = Collections.synchronizedMap(new HashMap<>());
		// Run tests and collect the results
		envs.sort(Comparator.comparing(Environment::getName));
		for (Environment env : envs) {
			System.out.println("Starting testing on " + env.getName());
			env.initialize(dataRoot, runnerRoot, false);
			TestResults results = env.runTests(runnerRoot, testsRoot, devMode, genDocs, jUnit, debug, verbosity, timeout, jvmArgs);
			if (results == null) {
				if (devMode) {
					// Nothing to report, it's the dev mode environment.
					System.exit(0);
					return;
				}
				System.err.println("The test environment '" + env.getName() + "' failed to produce test results.");
				System.exit(3);
				return;
			}
			collectedResults.put(env, results);
		}

		// Process collected results
		for (var entry : collectedResults.entrySet()) {
			TestResults results = entry.getValue();
			Environment env = entry.getKey();
			docsFailed |= results.docsFailed();
			allTests.addAll(results.getSucceeded());
			allTests.addAll(results.getFailed().keySet());
			for (Map.Entry<String, String> fail : results.getFailed().entrySet()) {
				String error = fail.getValue();
				assert error != null;
				failures.computeIfAbsent(fail.getKey(), (k) -> new ArrayList<>())
					.add(new TestError(env, error));
			}
		}

		if (docsFailed) {
			System.err.println("Documentation templates not found. Cannot generate docs!");
			System.exit(2);
			return;
		}

		// Task was to generate docs, no test results other than docsFailed.
		if (genDocs) {
			System.exit(0);
			return;
		}

		// Sort results in alphabetical order
		List<String> succeeded = allTests.stream().filter(name -> !failures.containsKey(name)).sorted().collect(Collectors.toList());
		List<String> failNames = new ArrayList<>(failures.keySet());
		Collections.sort(failNames);

		// All succeeded tests in a single line
		StringBuilder output = new StringBuilder(String.format("%s Results %s%n", StringUtils.repeat("-", 25), StringUtils.repeat("-", 25)));
		output.append("\nTested environments: ").append(envs.stream().map(Environment::getName).collect(Collectors.joining(", ")));
		output.append("\nSucceeded:\n  ").append(String.join((jUnit ? "\n  " : ", "), succeeded));

		if (!failNames.isEmpty()) { // More space for failed tests, they're important
			output.append("\nFailed:");
			for (String failed : failNames) {
				List<TestError> errors = failures.get(failed);
				output.append("\n  ").append(failed).append(" (on ").append(errors.size()).append(" environment").append(errors.size() == 1 ? "" : "s").append(")");
				for (TestError error : errors) {
					output.append("\n    ").append(error.message()).append(" (on ").append(error.environment().getName()).append(")");
				}
			}
			output.append(String.format("%n%n%s", StringUtils.repeat("-", 60)));
			System.err.print(output);
			System.exit(failNames.size()); // Error code to indicate how many tests failed.
			return;
		}
		output.append(String.format("%n%n%s", StringUtils.repeat("-", 60)));
		System.out.print(output);
	}

	private record TestError(Environment environment, String message) { }

}
