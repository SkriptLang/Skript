package org.skriptlang.skript.bukkit.command.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandExecutionEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Fail Command Execution")
@Description("Causes a command to fail execution with an error message displayed to the executor.")
@Example("""
	command /withdraw <number>:
		trigger:
			if the number argument is greater than the player's balance:
				fail the command execution with the error message "Your balance is less than $%number argument%!"
	""")
@Since("INSERT VERSION")
public class EffFailExecution extends Effect implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT,
			SyntaxInfo.simple(EffFailExecution.class, EffFailExecution::new,
				"fail [the] command execution with [the] error [message] %textcomponent%"));
	}

	private Expression<Component> failureMessage;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		this.failureMessage = (Expression<Component>) exprs[0];
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(ScriptCommandExecutionEvent.class);
	}

	@Override
	protected void execute(Event event) { }

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		Component failureMessage = this.failureMessage.getSingle(event);
		if (!(event instanceof ScriptCommandExecutionEvent commandEvent) || failureMessage == null) {
			return super.walk(event);
		}
		debug(event, true);
		commandEvent.setFailureMessage(failureMessage);
		return null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "fail the command execution with the error message " + failureMessage.toString(event, debug);
	}

}
