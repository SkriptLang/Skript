package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import java.util.Arrays;
import java.util.List;

import ch.njol.skript.util.ColorRGB;
import org.bukkit.FireworkEffect;
import org.bukkit.event.Event;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.Nullable;


public class EvtFirework extends SkriptEvent {
	
	static {
		if (Skript.classExists("org.bukkit.event.entity.FireworkExplodeEvent"))
			//Making the event argument type fireworkeffects, led to Skript having troubles parsing for some reason.
			Skript.registerEvent("Firework Explode", EvtFirework.class, FireworkExplodeEvent.class, "[a] firework explo(d(e|ing)|sion) [colo[u]red %-colors%]")
					.description("Called when a firework explodes.")
					.examples("on firework explode:",
								"\tif event-colors contains red:",
							"on firework exploding colored red, light green and black:",
							"on firework explosion colored rgb 0, 255, 0:",
								"\tbroadcast \"A firework colored %colors% was exploded at %location%!\"")
					.since("2.4");
	}

	private @Nullable Literal<Color> colors;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null)
			colors = (Literal<Color>) args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof FireworkExplodeEvent fireworkExplodeEvent))
			return false;

		if (colors == null)
			return true;

		List<org.bukkit.Color> colours = colors.stream(event)
			.map(color -> {
				if (color instanceof ColorRGB)
					return color.asBukkitColor();
				return color.asDyeColor().getFireworkColor();
			})
			.toList();
		FireworkMeta meta = fireworkExplodeEvent.getEntity().getFireworkMeta();
		for (FireworkEffect effect : meta.getEffects()) {
			if (colours.containsAll(effect.getColors()))
				return true;
		}
		return false;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "Firework explode " + (colors != null ? " with colors " + colors.toString(e, debug) : "");
	}

}
