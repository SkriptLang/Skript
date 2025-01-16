package org.skriptlang.skript.bukkit.displays.text;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;

@Name("Text Display Has Drop Shadow")
@Description("Returns whether the text of a display has drop shadow applied.")
@Examples({
	"if {_display} has drop shadow:",
		"\tremove drop shadow from the text of {_display}"
})
@Since("2.10")
public class CondTextDisplayHasDropShadow extends PropertyCondition<Display> {

	static {
		Skript.registerCondition(CondTextDisplayHasDropShadow.class,
				"[[the] text of] %displays% (has|have) [a] (drop|text) shadow",
				"%displays%'[s] text (has|have) [a] (drop|text) shadow",
				"[[the] text of] %displays% (doesn't|does not|do not|don't) have [a] (drop|text) shadow",
				"%displays%'[s] text (doesn't|does not|do not|don't) have [a] (drop|text) shadow"
			);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!super.init(expressions, matchedPattern, isDelayed, parseResult))
			return false;
		setNegated(matchedPattern > 1);
		return true;
	}

	@Override
	public boolean check(Display value) {
		return value instanceof TextDisplay textDisplay && textDisplay.isShadowed();
	}

	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.HAVE;
	}

	@Override
	protected String getPropertyName() {
		return "drop shadow";
	}

}
