package org.skriptlang.skript.bukkit.displays.text;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Text Display Drop Shadow")
@Description("Applies or removes drop shadow from the displayed text on a text display.")
@Examples({
	"apply drop shadow to last spawned text display",
	"if {_display} has drop shadow:",
		"\tremove drop shadow from the text of {_display}"
})
@Since("2.10")
public class EffTextDisplayDropShadow extends Effect {

	static {
		Skript.registerEffect(EffTextDisplayDropShadow.class,
				"(apply|add) (drop|text) shadow to [[the] text of] %displays%",
				"(apply|add) (drop|text) shadow to %displays%'[s] text",
				"(remove|clear) (drop|text) shadow from [[the] text of] %displays%",
				"(remove|clear) (drop|text) shadow from %displays%'[s] text"
			);
	}

	private Expression<Display> displays;
	private boolean addShadow;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		displays = (Expression<Display>) expressions[0];
		addShadow = matchedPattern <= 1;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Display display : displays.getArray(event)) {
			if (display instanceof TextDisplay textDisplay)
				textDisplay.setShadowed(addShadow);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (addShadow)
			return "add drop shadow to " + displays.toString(event, debug);
		return "remove drop shadow from " + displays.toString(event, debug);
	}

}
