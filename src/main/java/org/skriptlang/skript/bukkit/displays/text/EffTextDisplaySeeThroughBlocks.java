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

@Name("Text Display See Through Blocks")
@Description("Forces a text display to either be or not be visible through blocks.")
@Examples({
	"force last spawned text display to be visible through walls",
	"prevent all text displays from being visible through walls"
})
@Since("2.10")
public class EffTextDisplaySeeThroughBlocks extends Effect {

	static {
		Skript.registerEffect(EffTextDisplaySeeThroughBlocks.class,
				"make %displays% visible through (blocks|walls)",
				"force %displays% to be visible through (blocks|walls)",
				"(prevent|block) %displays% from being (visible|seen) through (blocks|walls)"
			);
	}

	Expression<Display> displays;
	boolean canSee;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		displays = (Expression<Display>) expressions[0];
		canSee = matchedPattern != 2;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Display display : displays.getArray(event)) {
			if (display instanceof TextDisplay textDisplay)
				textDisplay.setSeeThrough(canSee);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (canSee)
			return "force " + displays.toString(event, debug) + " to be visible through blocks";
		return "prevent " + displays.toString(event, debug) + " from being visible through blocks";
	}

}
