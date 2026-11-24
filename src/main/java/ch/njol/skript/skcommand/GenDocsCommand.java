package ch.njol.skript.skcommand;

import ch.njol.skript.Skript;
import ch.njol.skript.skcommand.SkriptCommand.SubCommand;
import ch.njol.skript.test.runner.TestMode;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.docs.BukkitDocumentationAdapter;
import org.skriptlang.skript.bukkit.entity.EntityData;
import org.skriptlang.skript.docs.DocumentationAdapter;
import org.skriptlang.skript.docs.DocumentationGenerator;

import java.io.File;
import java.util.List;

/**
 * Internal command for generating docs
 */
class GenDocsCommand extends SubCommand {

	public GenDocsCommand() {
		super("gen-docs");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		// determine addon to generate for
		SkriptAddon addon;
		if (args.length == 2) {
			addon = Skript.instance().addon(args[1]);
			if (addon == null) {
				Skript.error(sender, "No addon by that name exists!");
				return;
			}
		} else {
			addon = Skript.instance();
		}

		// generate
		File outputDir = TestMode.getDocsOutputDirectory();
		if (!outputDir.exists() && !outputDir.mkdirs()) {
			Skript.error(sender, "Failed to create necessary directory: " + outputDir);
			return;
		}

		Skript.info(sender, "Generating docs...");

		DocumentationAdapter documentationAdapter = BukkitDocumentationAdapter.of(addon);
		// write Bukkit-specific registries
		EntityData.write(documentationAdapter);

		// exclude statements (not useful for our purposes)
		documentationAdapter.deleteScope("statements");

		DocumentationGenerator.json(documentationAdapter)
			.generate(outputDir.toPath().resolve(addon.name() + "-docs.json"));

		Skript.info(sender, "All documentation generated!");
	}

	@Override
	public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		return List.of("gen-docs");
	}

}
