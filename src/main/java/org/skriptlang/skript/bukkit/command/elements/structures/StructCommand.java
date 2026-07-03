package org.skriptlang.skript.bukkit.command.elements.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandRegistrar;
import org.skriptlang.skript.bukkit.command.custom.ScriptBrigadierCommand;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandEvent;
import org.skriptlang.skript.bukkit.command.elements.structures.util.SubCommandEntryData;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.concurrent.atomic.AtomicBoolean;

public class StructCommand extends Structure {

	public static void register(SkriptAddon addon, SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.STRUCTURE,SyntaxInfo.Structure.builder(StructCommand.class)
			.supplier(StructCommand::new)
			.addPattern("command <.+>")
			.build());

		EventValueRegistry evRegistry = addon.registry(EventValueRegistry.class);
		evRegistry.register(EventValue.simple(ScriptCommandEvent.class, CommandSender.class, ScriptCommandEvent::getSender));
	}

	private static final SubCommandEntryData ROOT_ENTRY_DATA =
		new SubCommandEntryData("command", false, false);

	private static final AtomicBoolean SYNC_COMMANDS = new AtomicBoolean();

	private SectionNode rootNode;
	private ScriptBrigadierCommand command;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, EntryContainer entryContainer) {
		rootNode = entryContainer.getSource();
		return true;
	}

	@Override
	public boolean load() {
		// parsing
		var result = ROOT_ENTRY_DATA.getValue(rootNode);
		if (result == null) { // parsing failed, entry will have emitted a specific error message
			return false;
		}
		if (result.arguments().size() != 1 || !(result.arguments().getFirst().build() instanceof LiteralCommandNode<CommandSourceStack> node)) {
			Skript.error("A command must have a name.");
			return false;
		}

		// validation
		ScriptBrigadierCommand existing = ScriptCommandRegistrar.getCommand(node.getLiteral());
		if (existing != null) {
			Skript.error("A command with the name /" + node.getLiteral() + " is already defined in the script '" +
				existing.script().nameAndPath() + ".sk'");
			return false;
		}

		// registration
		command = new ScriptBrigadierCommand(getParser().getCurrentScript(), node, result.aliases(), result.description(), result.prefix());
		ScriptCommandRegistrar.register(command);
		SYNC_COMMANDS.set(true);

		return true;
	}

	@Override
	public boolean postLoad() {
		if (SYNC_COMMANDS.compareAndSet(true, false)) {
			ScriptCommandRegistrar.processRegistrations();
		}
		return true;
	}

	@Override
	public void unload() {
		ScriptCommandRegistrar.unregister(command);
		SYNC_COMMANDS.set(true);
	}

	@Override
	public void postUnload() {
		if (SYNC_COMMANDS.compareAndSet(true, false)) {
			ScriptCommandRegistrar.processUnregistrations();
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "command";
	}

}
