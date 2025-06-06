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
import org.skriptlang.skript.lang.experiment.ExperimentSet;
import org.skriptlang.skript.lang.script.Script;

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
				"%script% is using %strings%",
				"%scripts% are using %strings%",
				"%script% is(n't| not) using %strings%",
				"%scripts% are(n't| not) using %strings%");
	}

	private Expression<String> names;
	private Expression<Script> scripts;

	@SuppressWarnings("null")
	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result) {
		//noinspection unchecked
		this.names = (Expression<String>) expressions[1];
		//noinspection unchecked
		this.scripts = (Expression<Script>) expressions[0];
		this.setNegated(pattern > 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		String[] array = names.getArray(event);
		if (array.length == 0)
			return true;
		boolean isUsing = true;
		for (Script script : this.scripts.getArray(event)) {
			ExperimentSet data = script.getData(ExperimentSet.class);
			if (data == null) {
				isUsing = false;
			} else {
				for (@NotNull String object : array) {
					isUsing &= data.hasExperiment(object);
				}
			}
		}
		return isUsing ^ this.isNegated();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String whether = scripts.isSingle()
				? (isNegated() ? "isn't" : "is")
				: (isNegated() ? "aren't" : "are");
		return scripts.toString(event, debug) + " "
				+ whether + " using " + names.toString(event, debug);
	}

}
