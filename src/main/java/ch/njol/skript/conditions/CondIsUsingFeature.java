package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.experiment.Experimented;

@SuppressWarnings("NotNullFieldNotInitialized")
@Name("Is Using Experimental Feature")
@Description("Checks whether a script is using an experimental feature by name.")
@Examples({"the script is using \"example feature\"",
		"on load:",
		"\tif the script is using \"example feature\":",
		"\t\tbroadcast \"You're using an experimental feature!\""})
@Since("2.9.0")
public class CondIsUsingFeature extends Condition {

	static {
		Skript.registerCondition(CondIsUsingFeature.class,
				"[the] [current] script is using %strings%",
				"[the] [current] script is(n't| not) using %strings%");
	}

	private Expression<String> names;
	private Experimented snapshot;

	@SuppressWarnings("null")
	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result) {
		//noinspection unchecked
		this.names = (Expression<String>) expressions[0];
		this.setNegated(pattern == 1);
		this.snapshot = this.getParser().experimentSnapshot();
		return true;
	}

	@Override
	public boolean check(Event event) {
		String[] array = names.getArray(event);
		if (array.length == 0)
			return true;
		boolean isUsing = true;
		for (@NotNull String object : array) {
			isUsing &= snapshot.hasExperiment(object);
		}
		return isUsing ^ this.isNegated();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the current script " + (isNegated() ? "isn't" : "is") + " using " + names.toString(event, debug);
	}

}
