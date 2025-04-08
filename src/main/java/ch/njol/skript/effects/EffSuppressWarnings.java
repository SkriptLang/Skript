package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.script.ScriptWarning;

@Name("Locally Suppress Warning")
@Description("Suppresses target warnings from the current script.")
@Examples({
	"locally suppress missing conjunction warnings",
	"suppress the variable save warnings"
})
@Since("2.3")
public class EffSuppressWarnings extends Effect {

	static {
		StringBuilder warnings = new StringBuilder();
		ScriptWarning[] values = ScriptWarning.values();
		for (int i = 0; i < values.length; i++) {
			if (i != 0)
				warnings.append('|');
			warnings.append(values[i].ordinal()).append(':').append(values[i].getPattern());
		}
		Skript.registerEffect(EffSuppressWarnings.class, "[local[ly]] suppress [the] (" + warnings + ") warning[s]");
	}

	private @UnknownNullability ScriptWarning warning;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!getParser().isActive()) {
			Skript.error("You can't suppress warnings outside of a script!");
			return false;
		}

		warning = ScriptWarning.values()[parseResult.mark];
		if (warning.isDeprecated()) {
			Skript.warning(warning.getDeprecationMessage());
		} else {
			getParser().getCurrentScript().suppressWarning(warning);
		}
		return true;
	}

	@Override
	protected void execute(Event event) { }

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "suppress " + warning.getWarningName() + " warnings";
	}

}
