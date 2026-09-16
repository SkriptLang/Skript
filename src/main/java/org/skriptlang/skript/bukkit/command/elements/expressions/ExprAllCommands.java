package org.skriptlang.skript.bukkit.command.elements.expressions;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandRegistrar;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("All Commands")
@Description("Returns all registered commands or all script commands.")
@Example("send \"Number of all commands: %size of all commands%\"")
@Example("send \"Number of all script commands: %size of all script commands%\"")
@Since("2.6")
public class ExprAllCommands extends SimpleExpression<String> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.simple(ExprAllCommands.class, ExprAllCommands::new, String.class,
				"[all [[of] the]|the] [registered] [:script] commands"));
	}

	private boolean scriptCommandsOnly;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		scriptCommandsOnly = parseResult.hasTag("script");
		return true;
	}

	@Override
	protected String[] get(Event e) {
		if (scriptCommandsOnly) {
			return ScriptCommandRegistrar.getCommands().stream()
				.map(command -> command.node().getLiteral())
				.toArray(String[]::new);
		}
		return Bukkit.getCommandMap().getKnownCommands().keySet().toArray(String[]::new);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "all " + (scriptCommandsOnly ? "script " : "") + "commands";
	}

}
