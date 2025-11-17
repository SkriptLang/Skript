package ch.njol.skript.skcommand;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.log.TestingLogHandler;
import ch.njol.skript.skcommand.SkriptCommand.SubCommand;
import ch.njol.skript.test.runner.SkriptTestEvent;
import ch.njol.skript.test.runner.TestMode;
import ch.njol.skript.test.runner.TestTracker;
import ch.njol.skript.test.utils.TestResults;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;

class TestCommand extends SubCommand {

	public TestCommand() {
		super("test");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		File scriptFile;
		if (args.length == 1) {
			scriptFile = TestMode.lastTestFile;
			if (scriptFile == null) {
				Skript.error(sender, "No test script has been run yet!");
				return;
			}
		} else {
			if (args[1].equalsIgnoreCase("all")) {
				scriptFile = TestMode.TEST_DIR.toFile();
			} else {
				scriptFile = ScriptCommand.getScriptFromArgs(sender, args, TestMode.TEST_DIR.toFile());
				TestMode.lastTestFile = scriptFile;
			}
		}

		if (scriptFile == null || !scriptFile.exists()) {
			Skript.error(sender, "Test script doesn't exist!");
			return;
		}

		// Close previous loggers before we create a new one
		// This prevents closing logger errors
		// timingLogHandler.close();
		// logHandler.close();

		TestingLogHandler errorCounter = new TestingLogHandler(Level.SEVERE).start();
		ScriptLoader.loadScripts(scriptFile, errorCounter)
			.thenAccept(scriptInfo ->
				// Code should run on server thread
				Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), () -> {
					Bukkit.getPluginManager().callEvent(new SkriptTestEvent()); // Run it
					ScriptLoader.unloadScripts(ScriptLoader.getLoadedScripts());

					// Get results and show them
					TestResults testResults = TestTracker.collectResults();
					String[] lines = testResults.createReport().split("\n");
					for (String line : lines) {
						Skript.info(sender, line);
					}

					// Log results to file
					Skript.info(sender, "Collecting results to " + TestMode.RESULTS_FILE);
					String results = new GsonBuilder()
						.setPrettyPrinting() // Easier to read lines
						.disableHtmlEscaping() // Fixes issue with "'" character in test strings going Unicode
						.create().toJson(testResults);
					try {
						Files.writeString(TestMode.RESULTS_FILE, results);
					} catch (IOException e) {
						// noinspection ThrowableNotThrown
						Skript.exception(e, "Failed to write test results.");
					}
				})
			);
	}

	@Override
	public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		return ScriptCommand.getScriptCommandTabCompletions(args);
	}

}
