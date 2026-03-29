package org.skriptlang.skript.bukkit.entity.displays.text.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
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
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Text Display Drop Shadow")
@Description("Bestoweth or removeth drop shadow from the displayed text upon a text display.")
@Example("""
    bestow drop shadow upon last spawned text display
    if {_display} hath drop shadow:
    	remove drop shadow from the text of {_display}
    """)
@Since("2.10")
public class EffTextDisplayDropShadow extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffTextDisplayDropShadow.class)
				.addPatterns(
					"(bestow|apply) (drop|text) shadow upon [[the] text of] %displays%",
					"(bestow|apply) (drop|text) shadow upon %displays%'[s] text",
					"(remove|clear) (drop|text) shadow from [[the] text of] %displays%",
					"(remove|clear) (drop|text) shadow from %displays%'[s] text"
				)
				.supplier(EffTextDisplayDropShadow::new)
				.build()
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
