package ch.njol.skript.skcommand;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Documentation;
import ch.njol.skript.doc.HTMLGenerator;
import ch.njol.skript.doc.JSONGenerator;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

/**
 * Internal command for generating docs
 */
class GenDocsCommand {

	static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(build("gen-docs"));
	}

	private static LiteralCommandNode<CommandSourceStack> build(String literal) {
		return Commands.literal(literal)
			.executes(context -> {
				CommandSender sender = context.getSource().getSender();
				File templateDir = Documentation.getDocsTemplateDirectory();
				File outputDir = Documentation.getDocsOutputDirectory();
				outputDir.mkdirs();

				Skript.info(sender, "Generating docs...");

				try {
					JSONGenerator.of(Skript.instance()).generate(outputDir.toPath().resolve("docs.json"));
				} catch (IOException e) {
					// noinspection ThrowableNotThrown
					Skript.exception(e, "Error while generating docs.");
				}

				if (!templateDir.exists()) {
					Skript.info(sender, "JSON-only documentation generated!");
					return SINGLE_SUCCESS;
				}

				HTMLGenerator htmlGenerator = new HTMLGenerator(templateDir, outputDir);
				htmlGenerator.generate(); // Try to generate docs... hopefully
				Skript.info(sender, "All documentation generated!");
				return SINGLE_SUCCESS;
			})
			.build();
	}

}
