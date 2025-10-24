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
import java.util.concurrent.CompletableFuture;
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
		Path envsRoot = Paths.get(args[3]);
		boolean devMode = "true".equals(args[4]);
		boolean genDocs = "true".equals(args[5]);
		boolean jUnit = "true".equals(args[6]);
		boolean debug = "true".equals(args[7]);
		String verbosity = args[8].toUpperCase(Locale.ENGLISH);
		long timeout = Long.parseLong(args[9]);
		if (timeout < 0)
			timeout = 0;
		boolean parallel = "true".equals(args[10]);
		Set<String> jvmArgs = Sets.newHashSet(Arrays.copyOfRange(args, 11, args.length));
		if (jvmArgs.stream().noneMatch(arg -> arg.contains("-Xmx")))
			jvmArgs.add("-Xmx5G");

		// Load environments
		List<Environment> envs;
		if (Files.isDirectory(envsRoot)) {
			envs = Files.walk(envsRoot).filter(path -> !Files.isDirectory(path))
					.map(path -> {
						try {
							return gson.fromJson(Files.readString(path), Environment.class);
						} catch (JsonSyntaxException | IOException e) {
							throw new RuntimeException(e);
						}
					}).collect(Collectors.toList());
		} else {
			envs = Collections.singletonList(gson.fromJson(Files.readString(envsRoot), Environment.class));
		}
		System.out.println("Test environments: " + envs.stream().map(Environment::getName).collect(Collectors.joining(", ")));
		
		Set<String> allTests = new HashSet<>();
		Map<String, List<TestError>> failures = new HashMap<>();
		
		boolean docsFailed = false;
		// Run tests and collect the results
		envs.sort(Comparator.comparing(Environment::getName));
		int parallelPort = 25600; // Port to start from for parallel tests, so the servers don't clash
		long finalTimeout = timeout;
		List<Runnable> tasks = new ArrayList<>();
		Map<Environment, TestResults> collectedResults = Collections.synchronizedMap(new HashMap<>());
		for (Environment env : envs) {
			int finalParallelPort = parallelPort;
			tasks.add(() -> {
				try {
					System.out.println("Starting testing on " + env.getName());
					env.initialize(dataRoot, runnerRoot, false, finalParallelPort);
					TestResults results = env.runTests(runnerRoot, testsRoot, devMode, genDocs, jUnit, debug, verbosity, finalTimeout, jvmArgs);
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
				} catch (IOException | InterruptedException e) {
					throw new RuntimeException(e);
				}
			});
			if (parallel) {
				parallelPort += 1; // Increment port for next environment
			}
		}

		// Execute tasks either in parallel or sequentially
		if (parallel) {
			List<CompletableFuture<Void>> futures = new ArrayList<>();
			for (Runnable task : tasks) {
				futures.add(CompletableFuture.runAsync(task));
			}
			// Wait for all to complete
			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		} else {
			for (Runnable task : tasks) {
				task.run();
			}
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
