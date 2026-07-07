package org.skriptlang.skript.bukkit.command.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Command Executor")
@Description("""
	The executor of a command.
	This differs from the command sender in that the executor may not be the thing that triggered/initiated the command.
	The executor of a command is often changed to be different from the command sender by using a vanilla command such as "/execute".
	""")
@Example("""
	command /balance:
		# This works if you do "/execute as <player> run balance"
		# It will send the output to the command sender, but it will be as if "<player>" was the thing executing it.
		send "Your balance is %{balance::%uuid of the executor%}%" to the sender
	""")
@Since("INSERT VERSION")
public class ExprCommandExecutor extends SimpleExpression<Entity> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.simple(ExprCommandExecutor.class, ExprCommandExecutor::new, Entity.class,
				"[the] [command['s]] executor"));
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		//noinspection unchecked
		return new Class[]{ScriptCommandEvent.class};
	}

	@Override
	protected Entity[] get(Event event) {
		Entity executor = null;
		if (event instanceof ScriptCommandEvent scriptCommandEvent) {
			executor = scriptCommandEvent.getExecutor();
		}
		return executor == null ? new Entity[0] : new Entity[]{executor};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the command executor";
	}

}
