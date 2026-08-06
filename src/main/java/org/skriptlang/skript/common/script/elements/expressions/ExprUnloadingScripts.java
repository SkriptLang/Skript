package org.skriptlang.skript.common.script.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.experiments.ReflectionExperimentSyntax;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.script.elements.events.EvtScripts.ScriptsUnloadEvent;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Unloading Scripts")
@Description("The scripts unloading in a 'scripts unloading' event.")
@Example("""
	on scripts unloading:
		broadcast "%the size of the unloading scripts% scripts were just unloaded!"
	""")
@Since("INSERT VERSION")
@Events("Scripts Loading/Unloading")
public class ExprUnloadingScripts extends SimpleExpression<Script> implements EventRestrictedSyntax, ReflectionExperimentSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.simple(ExprUnloadingScripts.class, ExprUnloadingScripts::new, Script.class,
				"[all [[of] the]|the] (unloading|stopping|disabling) scripts"));
	}
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		//noinspection unchecked
		return new Class[]{ScriptsUnloadEvent.class};
	}

	@Override
	protected Script @Nullable [] get(Event event) {
		return ((ScriptsUnloadEvent) event).scripts.toArray(new Script[0]);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends Script> getReturnType() {
		return Script.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the unloading scripts";
	}

}
