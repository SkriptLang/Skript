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
import org.skriptlang.skript.common.script.elements.events.EvtScripts.ScriptsLoadEvent;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Loading Scripts")
@Description("The scripts loading in a 'scripts loading' event.")
@Example("""
	on scripts loading:
		broadcast "%the size of the loading scripts% scripts were just loaded!"
	""")
@Since("INSERT VERSION")
@Events("Scripts Loading/Unloading")
public class ExprLoadingScripts extends SimpleExpression<Script> implements EventRestrictedSyntax, ReflectionExperimentSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.simple(ExprLoadingScripts.class, ExprLoadingScripts::new, Script.class,
				"[all [[of] the]|the] (loading|initializing|enabling) scripts"));
	}
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		//noinspection unchecked
		return new Class[]{ScriptsLoadEvent.class};
	}

	@Override
	protected Script @Nullable [] get(Event event) {
		return ((ScriptsLoadEvent) event).scripts.toArray(new Script[0]);
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
		return "the loading scripts";
	}

}
