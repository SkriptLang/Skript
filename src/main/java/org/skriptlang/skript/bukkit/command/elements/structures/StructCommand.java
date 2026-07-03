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
import org.skriptlang.skript.bukkit.command.brigadier.RuntimeCommandRegistrar;
import org.skriptlang.skript.bukkit.command.brigadier.RuntimeCommandRegistrar.CommandRegistration;
import org.skriptlang.skript.bukkit.command.brigadier.ScriptCommandEvent;
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
	private CommandRegistration command;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, EntryContainer entryContainer) {
		rootNode = entryContainer.getSource();
		return true;
	}

	@Override
	public boolean load() {
		var result = ROOT_ENTRY_DATA.getValue(rootNode);
		if (result == null) { // parsing failed, entry will have emitted a specific error message
			return false;
		}
		if (result.arguments().size() != 1 || !(result.arguments().getFirst().build() instanceof LiteralCommandNode<CommandSourceStack> node)) {
			Skript.error("A command must have a name.");
			return false;
		}
		command = new CommandRegistration(node, result.aliases(), result.description(), result.prefix());
		// TODO validate whether command already exists

		RuntimeCommandRegistrar.register(command);
		SYNC_COMMANDS.set(true);

		return true;
	}

	@Override
	public boolean postLoad() {
		if (SYNC_COMMANDS.compareAndSet(true, false)) {
			RuntimeCommandRegistrar.processRegistrations();
		}
		return true;
	}

	@Override
	public void unload() {
		RuntimeCommandRegistrar.unregister(command);
		SYNC_COMMANDS.set(true);
	}

	@Override
	public void postUnload() {
		if (SYNC_COMMANDS.compareAndSet(true, false)) {
			RuntimeCommandRegistrar.processUnregistrations();
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "command";
	}

}
