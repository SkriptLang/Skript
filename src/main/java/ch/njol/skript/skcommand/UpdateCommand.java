package ch.njol.skript.skcommand;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptUpdater;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

/**
 * Checks if the installed Skript version is up to date<p>
 * Usage: {@code /sk update}
 */
class UpdateCommand {

	static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(build("update"));
	}

	private static LiteralCommandNode<CommandSourceStack> build(String literal) {
		return Commands.literal(literal)
			.executes(context -> {
				CommandSender sender = context.getSource().getSender();
				SkriptUpdater updater = Skript.getInstance().getUpdater();
				if (updater == null) { // Oh. That is bad
					Skript.info(sender, "" + SkriptUpdater.m_internal_error);
					return SINGLE_SUCCESS;
				}

				updater.updateCheck(sender);

				// TODO - add a dialog showing changes?
				return SINGLE_SUCCESS;
			})
			.build();
	}

}
