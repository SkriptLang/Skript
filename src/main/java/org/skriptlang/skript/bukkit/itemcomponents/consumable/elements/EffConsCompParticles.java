package org.skriptlang.skript.bukkit.itemcomponents.consumable.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumableWrapper;

@Name("Consumable Component - Particles")
@Description("""
	Whether an item should have particles enabled when being consumed.
	NOTE: Consumable component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	if {_item} does not have consumption particles enabled:
		enable the consumption particles of {_item}
	""")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
public class EffConsCompParticles extends Effect implements ConsumableExperimentSyntax {

	static {
		Skript.registerEffect(EffConsCompParticles.class,
			"(enable|:disable) [the] consum(e|ption) particle[s] [effect[s]] (of|for) %consumablecomponents%");
	}

	private Expression<ConsumableWrapper> wrappers;
	private boolean enable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		wrappers = (Expression<ConsumableWrapper>) exprs[0];
		enable = !parseResult.hasTag("disable");
		return true;
	}

	@Override
	protected void execute(Event event) {
		wrappers.stream(event).forEach(wrapper ->
			wrapper.editBuilder(builder -> builder.hasConsumeParticles(enable)));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (enable) {
			builder.append("enable");
		} else {
			builder.append("disable");
		}
		builder.append("the consume particle effects of", wrappers);
		return builder.toString();
	}

}
