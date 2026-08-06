package ch.njol.skript.skcommand;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Documentation;
import ch.njol.skript.doc.HTMLGenerator;
import ch.njol.skript.doc.JSONGenerator;
import ch.njol.skript.skcommand.SkriptCommand.SubCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
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
		File templateDir = Documentation.getDocsTemplateDirectory();
		File outputDir = Documentation.getDocsOutputDirectory();
		outputDir.mkdirs();

		Skript.info(sender, "Generating docs...");

		try {
			JSONGenerator.of(Skript.instance())
				.generate(outputDir.toPath().resolve("docs.json"));
		} catch (IOException e) {
			// noinspection ThrowableNotThrown
			Skript.exception(e, "Error while generating docs.");
		}

		if (!templateDir.exists()) {
			Skript.info(sender, "JSON-only documentation generated!");
			return;
		}

		HTMLGenerator htmlGenerator = new HTMLGenerator(templateDir, outputDir);
		htmlGenerator.generate(); // Try to generate docs... hopefully
		Skript.info(sender, "All documentation generated!");
	}

	@Override
	public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		return List.of();
	}

}
