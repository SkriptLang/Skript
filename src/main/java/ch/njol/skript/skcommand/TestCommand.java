package ch.njol.skript.skcommand;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.log.TestingLogHandler;
import ch.njol.skript.test.runner.SkriptTestEvent;
import ch.njol.skript.test.runner.TestMode;
import ch.njol.skript.test.runner.TestTracker;
import ch.njol.skript.test.utils.TestResults;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

/**
 * Internal command for running tests
 */
class TestCommand {

	static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(build("test"));
	}

	private static LiteralCommandNode<CommandSourceStack> build(String literal) {
		return Commands.literal(literal)
			.executes(context -> {
				CommandSender sender = context.getSource().getSender();
				File scriptFile = TestMode.lastTestFile;
				if (scriptFile == null) {
					Skript.error(sender, "No test script has been run yet!");
					return SINGLE_SUCCESS;
				}
				runTest(sender, scriptFile);
				return SINGLE_SUCCESS;
			})
			.then(Commands.literal("all")
				.executes(context -> {
					runTest(context.getSource().getSender(), TestMode.TEST_DIR.toFile());
					return SINGLE_SUCCESS;
				}))
			.then(Commands.argument("script", StringArgumentType.greedyString())
				.suggests(ScriptCommandUtils.suggestions(true, false))
				.executes(context -> {
					CommandSender sender = context.getSource().getSender();
					String scriptName = StringArgumentType.getString(context, "script").trim();
					File scriptFile = ScriptCommandUtils.getScriptFileFromName(sender, scriptName, TestMode.TEST_DIR.toFile());
					TestMode.lastTestFile = scriptFile;
					runTest(sender, scriptFile);
					return SINGLE_SUCCESS;
				}))
			.build();
	}

	private static void runTest(CommandSender sender, @Nullable File scriptFile) {
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

}
