package org.skriptlang.skript.bukkit.command.elements.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.command.elements.structures.util.ArgumentData;
import org.skriptlang.skript.bukkit.command.elements.structures.util.CommandEvent;
import org.skriptlang.skript.bukkit.command.elements.structures.util.SkriptBrigadierArgument;
import org.skriptlang.skript.bukkit.command.elements.structures.util.SkriptCommandExecutor;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.TriggerEntryData;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StructCommand extends Structure {

	public static void register(SkriptAddon addon, SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.STRUCTURE,SyntaxInfo.Structure.builder(StructCommand.class)
			.supplier(StructCommand::new)
			.addPattern("command [/]<\\S+> [<.+>]")
			.entryValidator(EntryValidator.builder()
				.addEntryData(new TriggerEntryData("trigger", null, false))
				.build())
			.build());

		EventValueRegistry evRegistry = addon.registry(EventValueRegistry.class);
		evRegistry.register(EventValue.simple(CommandEvent.class, CommandSender.class, CommandEvent::getSender));

		Skript.getInstance().getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,commands -> {
			var registrar = commands.registrar();
			COMMANDS.forEach(registrar::register);
		});
	}

	private static final Set<LiteralCommandNode<CommandSourceStack>> COMMANDS = ConcurrentHashMap.newKeySet();
	private static final AtomicBoolean SYNC_COMMANDS = new AtomicBoolean();

	private static void performSync() {
		if (SYNC_COMMANDS.get()) {
			SYNC_COMMANDS.set(false);
			Bukkit.reloadData();
		}
	}

	/**
	 * Pattern for parsing arguments in the form: {@code <name: type = defaultValue>}.
	 * <br>
	 * Where {@code name}, {@code type}, and {@code defaultValue} are groups {@code 1}, {@code 2}, and {@code 3}, respectively.
	 */
	private static final Pattern ARGUMENT_PATTERN =
		Pattern.compile("<\\s*(?:([^>]+?)\\s*:\\s*)?(.+?)\\s*(?:=\\s*(" + SkriptParser.WILDCARD + "))?\\s*>");

	private String name;
	private EntryContainer entryContainer;
	private final List<ArgumentData<?>> arguments = new ArrayList<>();

	private LiteralCommandNode<CommandSourceStack> command;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, EntryContainer entryContainer) {
		name = parseResult.regexes.getFirst().group();
		this.entryContainer = entryContainer;

		if (parseResult.regexes.size() == 1) { // no arguments specified
			return true;
		}

		// parse arguments
		Matcher argumentMatcher = ARGUMENT_PATTERN.matcher(parseResult.regexes.getLast().group());
		boolean optional = false;
		while (argumentMatcher.find()) {
			// first, parse the type
			String rawType = argumentMatcher.group(2);
			var plural = Utils.isPlural(rawType);
			ClassInfo<?> type = Classes.getClassInfoFromUserInput(plural.updated());
			if (type == null) {
				Skript.error("'" + rawType + "' is not a known type.");
				return false;
			} else if (type.getParser() == null || !type.getParser().canParse(ParseContext.COMMAND)) {
				Skript.error("The type '" + type.getName().getSingular() + "' cannot be used as a command argument.");
				return false;
			}

			// next, parse the name
			String name = argumentMatcher.group(1);
			boolean isAutomaticName = false;
			if (name == null) { // user did not specify, manually create one
				isAutomaticName = true;
				name = type.getName().getSingular();
				String finalName = name;
				if (arguments.stream().anyMatch(data -> data.name().equals(finalName))) { // already taken
					// try to generate a name like 'number2', 'number3', etc
					int i = 2;
					baseLoop: while (true) {
						for (var argument : arguments) {
							if (argument.name().equals(name + i)) {
								i++;
								continue baseLoop;
							}
						}
						break;
					}
					name += i;
				}
			}

			// finally, parse the default value
			Expression<?> defaultValue = null;
			String rawDefaultValue = argumentMatcher.group(3);
			if (rawDefaultValue != null) {
				int parseType;
				if (rawDefaultValue.startsWith("%") && rawDefaultValue.endsWith("%")) {
					parseType = SkriptParser.PARSE_EXPRESSIONS;
					rawDefaultValue = rawDefaultValue.substring(1, rawDefaultValue.length() - 1);
				} else {
					parseType = SkriptParser.PARSE_LITERALS;
				}
				try (var logHandler = new RetainingLogHandler().start()) {
					defaultValue = new SkriptParser(rawDefaultValue, parseType, ParseContext.COMMAND)
						.parseExpression(type.getC());
					if (defaultValue == null) {
						logHandler.printErrors("Can't understand this expression: '" + rawDefaultValue + "'."
							+ " The default value will be ignored for this argument.");
					} else {
						logHandler.printLog();
					}
				}
			}
			optional |= defaultValue != null;

			//noinspection unchecked, rawtypes
			arguments.add(new ArgumentData(name, isAutomaticName, type, defaultValue, optional));
		}

		return true;
	}

	@Override
	public boolean load() {
		ParserInstance parser = getParser();
		parser.setCurrentEvent("command execution trigger", CommandEvent.class);
		Trigger execute = entryContainer.get("trigger", Trigger.class, false);
		parser.deleteCurrentEvent();

		var builder = Commands.literal(name);

		List<ArgumentBuilder<CommandSourceStack, ?>> pieces = new LinkedList<>();
		pieces.addFirst(builder);
		for (int i = 0; i < arguments.size(); i++) {
			ArgumentData<?> argument = arguments.get(i);
			SkriptBrigadierArgument<?> brigadierArgument;
			if (i == arguments.size() - 1) {
				// TODO maybe not desirable for numbers
				brigadierArgument = new SkriptBrigadierArgument.GreedyArgument<>(argument);
			} else {
				brigadierArgument = new SkriptBrigadierArgument<>(argument);
			}
			pieces.addFirst(Commands.argument(argument.name(), brigadierArgument));
		}

		// setup base
		ArgumentBuilder<CommandSourceStack, ?> base = pieces.removeFirst();
		SkriptCommandExecutor executor = new SkriptCommandExecutor(execute, arguments);
		int argCount = arguments.size();
		final int finalBaseArgCount = argCount;
		base.executes(context -> executor.execute(context, finalBaseArgCount));

		// attach rest of argument pieces
		var iterator = pieces.iterator();
		// wasOptional is so that we place an executes on the last, non-optional argument
		boolean wasOptional = arguments.getLast().optional();
		while (iterator.hasNext()) {
			argCount--;
			final int finalArgCount = argCount;
			base = iterator.next().then(base);
			if (base instanceof RequiredArgumentBuilder<?,?> requiredArgument &&
				((SkriptBrigadierArgument<?>) requiredArgument.getType()).getArgument().optional()) {
				base.executes(context -> executor.execute(context, finalArgCount));
				wasOptional = true;
			} else if (wasOptional) {
				base.executes(context -> executor.execute(context, finalArgCount));
				wasOptional = false;
			}
			iterator.remove();
		}

		command = builder.build();

		COMMANDS.add(command);
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
		return "command /" + name;
	}

}
