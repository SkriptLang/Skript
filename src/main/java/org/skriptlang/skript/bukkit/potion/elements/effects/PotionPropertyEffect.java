package org.skriptlang.skript.bukkit.potion.elements.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;

/**
 * A utility {@link Effect} class for handling and ensuring consistency of the common components
 *  of syntax for {@link SkriptPotionEffect} properties.
 */
abstract class PotionPropertyEffect extends Effect {

	/**
	 * Type of property effect. Describes the action of changing the potion property.
	 */
	public enum Type {
		MAKE, SHOW
	}

	/**
	 * Constructs and returns patterns for a potion property effect.
	 * @param type The type of property effect.
	 * @param property The potion property.
	 * @return Patterns completed by the provided {@code type} and {@code property}.
	 */
	public static String[] getPatterns(Type type, String property) {
		return switch (type) {
			case MAKE -> new String[]{
				"make %skriptpotioneffects% [:not] " + property,
			};
			case SHOW -> new String[]{
				"(show|not:hide) [the] [potion] " + property + " [(of|for) %skriptpotioneffects%]",
				"(show|not:hide) %skriptpotioneffects%'[s] " + property,
			};
		};
	}

	private Expression<SkriptPotionEffect> potions;

	private boolean isNegated;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		potions = (Expression<SkriptPotionEffect>) expressions[0];
		isNegated = parseResult.hasTag("not");
		return SkriptPotionEffect.isChangeable(potions);
	}

	@Override
	protected void execute(Event event) {
		for (SkriptPotionEffect potionEffect : potions.getArray(event)) {
			modify(potionEffect, isNegated);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		switch (getPropertyType()) {
			case MAKE -> {
				builder.append("make", potions);
				if (isNegated) {
					builder.append("not");
				}
				builder.append(getPropertyName());
			}
			case SHOW -> {
				if (isNegated) {
					builder.append("hide");
				} else {
					builder.append("show");
				}
				builder.append("the potion", getPropertyName(), "of", potions);
			}
		}
		return builder.toString();
	}

	/**
	 * Modifies a property of a potion effect.
	 * @param effect The effect to modify.
	 * @param isNegated Whether the property is being negated (e.g., "not X").
	 */
	public abstract void modify(SkriptPotionEffect effect, boolean isNegated);

	/**
	 * @return The type of property effect.
	 */
	public abstract Type getPropertyType();

	/**
	 * @return A name representing the potion property.
	 */
	public abstract String getPropertyName();

}
