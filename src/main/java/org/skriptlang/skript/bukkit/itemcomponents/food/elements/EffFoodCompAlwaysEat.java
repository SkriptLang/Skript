package org.skriptlang.skript.bukkit.itemcomponents.food.elements;

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
import org.skriptlang.skript.bukkit.itemcomponents.food.FoodExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.food.FoodWrapper;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Food Component - Always Be Eaten")
@Description("""
	Whether an item should be eaten when the player's hunger bar is full.
	NOTE: Food component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	if {_item} can not always be eaten:
		allow {_item} to always be eaten
	""")
@Example("""
	set {_component} to the food component of {_item}
	if {_component} can be eaten when full:
		prevent {_component} from being eaten when full
	""")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
public class EffFoodCompAlwaysEat extends Effect implements FoodExperimentalSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffFoodCompAlwaysEat.class)
				.addPatterns(
					"(allow|force) %foodcomponents% to (always be eaten|be eaten when full)",
					"prevent %foodcomponents% from (always being eaten|being eaten when full)"
				)
				.supplier(EffFoodCompAlwaysEat::new)
				.build()
		);
	}

	private boolean alwaysEat;
	private Expression<FoodWrapper> wrappers;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		alwaysEat = matchedPattern == 0;
		//noinspection unchecked
		wrappers = (Expression<FoodWrapper>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		wrappers.stream(event).forEach(wrapper -> wrapper.editBuilder(builder -> {
			builder.canAlwaysEat(alwaysEat);
		}));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (alwaysEat) {
			builder.append("allow", wrappers, "to always be eaten");
		} else {
			builder.append("prevent", wrappers, "from always being eaten");
		}
		return builder.toString();
	}

}
