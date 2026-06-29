package org.skriptlang.skript.bukkit.command.elements.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.command.elements.structures.util.CommandEvent;
import org.skriptlang.skript.bukkit.command.elements.structures.util.SubCommandEntryData;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class StructCommand extends Structure {

	public static void register(SkriptAddon addon, SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.STRUCTURE,SyntaxInfo.Structure.builder(StructCommand.class)
			.supplier(StructCommand::new)
			.addPattern("command <.+>")
			.build());

		EventValueRegistry evRegistry = addon.registry(EventValueRegistry.class);
		evRegistry.register(EventValue.simple(CommandEvent.class, CommandSender.class, CommandEvent::getSender));

		Skript.getInstance().getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,commands -> {
			var registrar = commands.registrar();
			COMMANDS.forEach(registrar::register);
		});
	}

	private static final SubCommandEntryData ROOT_ENTRY_DATA =
		new SubCommandEntryData("command", false, false);

	private static final Set<LiteralCommandNode<CommandSourceStack>> COMMANDS = ConcurrentHashMap.newKeySet();
	private static final AtomicBoolean SYNC_COMMANDS = new AtomicBoolean();

	private static void performSync() {
		if (SYNC_COMMANDS.getAndSet(false)) {
			Bukkit.reloadData();
		}
	}

	private SectionNode rootNode;
	private LiteralCommandNode<CommandSourceStack> command;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, EntryContainer entryContainer) {
		rootNode = entryContainer.getSource();
		return true;
	}

	@Override
	public boolean load() {
		var command = ROOT_ENTRY_DATA.getValue(rootNode);
		if (command == null) {
			return false;
		}
		if (command.size() != 1 || !(command.getFirst().build() instanceof LiteralCommandNode<CommandSourceStack> commandNode)) {
			Skript.error("A command must have a name.");
			return false;
		}
		this.command = commandNode;
		// TODO validate whether command already exists

		COMMANDS.add(this.command);
		SYNC_COMMANDS.set(true);

		return true;
	}

	@Override
	public boolean postLoad() {
		performSync();
		return true;
	}

	@Override
	public void unload() {
		COMMANDS.remove(command);
		SYNC_COMMANDS.set(true);
	}

	@Override
	public void postUnload() {
		performSync();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "command";
	}

}
