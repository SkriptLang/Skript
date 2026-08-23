package org.skriptlang.skript.bukkit.types;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.FireworkEffect;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

import java.util.List;

@ApiStatus.Internal
public class FireworkEffectClassInfo extends ClassInfo<FireworkEffect> {

	public FireworkEffectClassInfo() {
		super(FireworkEffect.class, "fireworkeffect");
		this.user("firework ?effects?")
			.name("Firework Effect")
			.usage("See <a href='/#FireworkType'>Firework Types</a>")
			.description(
				"A configuration of effects that defines the firework when exploded",
				"which can be used in the <a href='#EffFireworkLaunch'>launch firework</a> effect.",
				"See the <a href='#ExprFireworkEffect'>firework effect</a> expression for detailed patterns."
			)
			.defaultExpression(new EventValueExpression<>(FireworkEffect.class))
			.examples(
				"launch flickering trailing burst firework colored blue and green at player",
				"launch trailing flickering star colored purple, yellow, blue, green and red fading to pink at target entity",
				"launch ball large colored red, purple and white fading to light green and black at player's location with duration 1"
			)
			.since("2.4")
			.parser(new FireworkEffectParser())
			.property(Property.COLOR,
				"The colors of a firework effect. Can be set, added to, removed from, reset and deleted.",
				Skript.instance(),
				new FireworkEffectColorHandler());
	}

	private static class FireworkEffectParser extends Parser<FireworkEffect> {
		//<editor-fold desc="firework effect parser" defaultstate="collapsed">
		@Override
		public boolean canParse(ParseContext context) {
			return false;
		}

		@Override
		public String toString(FireworkEffect effect, int flags) {
			return "Firework effect " + effect.toString();
		}

		@Override
		public String toVariableNameString(FireworkEffect effect) {
			return "firework effect " + effect.toString();
		}
		//</editor-fold>
	}

	private static class FireworkEffectColorHandler implements ExpressionPropertyHandler<FireworkEffect, Object> {
		//<editor-fold desc="color property for firework effects" defaultstate="collapsed">
		@Override
		public Color[] convert(FireworkEffect effect) {
			return effect.getColors().stream()
				.map(ColorRGB::fromBukkitColor)
				.toArray(Color[]::new);
		}

		@Override
		public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
			return switch (mode) {
				case SET, ADD, REMOVE, REMOVE_ALL, DELETE, RESET -> CollectionUtils.array(Color[].class);
				default -> null;
			};
		}

		@Override
		public void change(FireworkEffect effect, Object @Nullable [] delta, ChangeMode mode) {
			List<org.bukkit.Color> colors = effect.getColors();
			switch (mode) {
				case DELETE, RESET -> colors.clear();
				case SET -> {
					colors.clear();
					addAll(colors, delta);
				}
				case ADD -> addAll(colors, delta);
				case REMOVE, REMOVE_ALL -> {
					if (delta == null)
						return;
					for (Object object : delta)
						colors.remove(((Color) object).asBukkitColor());
				}
				default -> {
				}
			}
		}

		private static void addAll(List<org.bukkit.Color> colors, Object @Nullable [] delta) {
			if (delta == null)
				return;
			for (Object object : delta)
				colors.add(((Color) object).asBukkitColor());
		}

		@Override
		public @NotNull Class<Object> returnType() {
			//noinspection rawtypes, unchecked
			return (Class) Color.class;
		}
		//</editor-fold>
	}

}
