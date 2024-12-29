package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.effects.EffExplosion;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.FireworkEffect;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Collectors;

public class EvtExplode extends SkriptEvent {

	static {
		Skript.registerEvent("Explode", EvtExplode.class,
				CollectionUtils.array(EffExplosion.ScriptExplodeEvent.class, FireworkExplodeEvent.class, EntityExplodeEvent.class),
				"[a] script[ed] explo(d(e|ing)|sion)",
				"[a] firework explo(d(e|ing)|sion) [colo[u]red %-colors%]",
				"[a] [%-entitytypes%] explo(d(e|ing)|sion)"
			)
			.description(
				"Called when an entity explodes, or when an explosion is created by a script.",
				"Entity and script explosions have a power value, obtained by using `event-number`.",
				"Fireworks have an optional specifier for the exploded color."
			)
			.examples(
				"on explosion:",
				"on script explosion:",
				"on tnt explosion:",
				"on firework explode:",
					"\tif event-colors contains red:",
				"on firework exploding colored red, light green and black:",
				"on firework explosion colored rgb 0, 255, 0:",
					"\tbroadcast \"A firework colored %colors% was exploded at %location%!\""
			)
			.since("1.0, INSERT VERSION (script)");
	}

	private int pattern;
	private @Nullable Literal<Color> colors;
	private @Nullable Literal<? extends EntityType> typesLiteral;
	private EntityType @Nullable [] types;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		pattern = matchedPattern;

		switch (matchedPattern) {
			case 1 -> {
				if (args[0] != null)
					//noinspection unchecked
					colors = (Literal<Color>) args[0];
			}
			case 2 -> {
				//noinspection unchecked
				Literal<? extends EntityType> arg = (Literal<? extends EntityType>) args[0];
				if (arg != null) {
					typesLiteral = arg;
					types = arg.getAll();
				}
			}
		}

		return true;
	}

	@Override
	public boolean check(Event event) {
		if (pattern == 0 && event instanceof EffExplosion.ScriptExplodeEvent) {
			return true;
		} else if (pattern == 1 && event instanceof FireworkExplodeEvent fireworkExplodeEvent) {
			if (colors == null)
				return true;

			Set<org.bukkit.Color> colours = colors.stream(event)
				.map(color -> {
					if (color instanceof ColorRGB)
						return color.asBukkitColor();
					return color.asDyeColor().getFireworkColor();
				})
				.collect(Collectors.toSet());

			FireworkMeta meta = fireworkExplodeEvent.getEntity().getFireworkMeta();
			for (FireworkEffect effect : meta.getEffects()) {
				if (colours.containsAll(effect.getColors()))
					return true;
			}
			return false;
		} else if (pattern == 2 && event instanceof EntityExplodeEvent explodeEvent) {
			if (types == null)
				return true;

			for (EntityType type : types) {
				if (type.isInstance(explodeEvent.getEntity()))
					return true;
			}
			return false;
		}

		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return switch (pattern) {
			case 0 -> "script explosion";
			case 1 -> "firework explode" + (colors != null ? " with colors " + colors.toString(event, debug) : "");
			case 2 -> typesLiteral != null ? typesLiteral.toString(event, debug) + " explosion" : "explosion";
			default -> "unknown explosion";
		};
	}

}
