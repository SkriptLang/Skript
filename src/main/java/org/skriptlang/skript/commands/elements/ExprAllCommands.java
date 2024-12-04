package org.skriptlang.skript.commands.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.commands.CommandModule;

@Name("All Commands")
@Description("An expression to obtain all registered commands or all registered script commands.")
@Examples({
	"send \"number of all commands: %size of all commands%\"",
	"send \"number of all script commands: %size of all script commands%\""
})
@Since("2.6")
public class ExprAllCommands extends SimpleExpression<String> {
	
	static {
		Skript.registerExpression(ExprAllCommands.class, String.class, ExpressionType.SIMPLE,
			"[all [[of] the]|the] [registered] [:script] commands"
		);
	}
	
	private boolean scriptCommands;
	
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		scriptCommands = parseResult.hasTag("script");
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event) {
		if (scriptCommands) {
			return CommandModule.getCommandHandler().getScriptCommands().toArray(new String[0]);
		}
		return CommandModule.getCommandHandler().getServerCommands().toArray(new String[0]);
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
		return "all of the registered" + (scriptCommands ? " script " : " ") + "commands";
	}
	
}
