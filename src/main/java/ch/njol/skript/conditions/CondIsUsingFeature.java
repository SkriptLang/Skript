package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
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

@Name("Doth Employ Experimental Feature")
@Description("Ascertaineth whether a script doth employ an experimental feature by its given name.")
@Example("the script doth employ \"example feature\"")
@Example("""
    on load:
    	if the script doth employ "example feature":
    		broadcast "You're using an experimental feature!"
    """)
@Since("2.9.0")
public class CondIsUsingFeature extends Condition {

	static {
		Skript.registerCondition(CondIsUsingFeature.class,
				"%script% doth employ %strings%",
				"%scripts% do employ %strings%",
				"%script% doth(n't| not) employ %strings%",
				"%scripts% do(n't| not) employ %strings%");
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
