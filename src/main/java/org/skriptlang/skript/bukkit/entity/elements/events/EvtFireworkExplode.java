package org.skriptlang.skript.bukkit.entity.elements.events;


import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.SkriptColor;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Firework;
import org.bukkit.event.Event;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EvtFireworkExplode extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtFireworkExplode.class, "Firework Explode")
			.supplier(EvtFireworkExplode::new)
			.addEvent(FireworkExplodeEvent.class)
			.addPatterns("[a] firework explo(d(e|ing)|sion) [colo[u]red %-colors%]")
			.addDescription("Called when a firework explodes.")
			.addExample("""
				on firework explode:
				    if event-colors contains red:
				        broadcast "its a red firework!"
				""")
			.addSince("2.4")
			.build());

		eventValueRegistry.register(EventValue.builder(FireworkExplodeEvent.class, Firework.class)
			.getter(FireworkExplodeEvent::getEntity)
			.build());

		eventValueRegistry.register(EventValue.builder(FireworkExplodeEvent.class, FireworkEffect.class)
			.getter(event -> {
				List<FireworkEffect> effects = event.getEntity().getFireworkMeta().getEffects();
				if (effects.isEmpty())
					return null;
				return effects.getFirst();
			})
			.build());

		eventValueRegistry.register(EventValue.builder(FireworkExplodeEvent.class, Color[].class)
			.getter(event -> {
				List<FireworkEffect> effects = event.getEntity().getFireworkMeta().getEffects();
				if (effects.isEmpty())
					return new Color[0];
				List<Color> colors = new ArrayList<>();
				for (FireworkEffect fireworkEffect : effects) {
					for (org.bukkit.Color color : fireworkEffect.getColors()) {
						if (SkriptColor.fromBukkitColor(color) != null) {
							colors.add(SkriptColor.fromBukkitColor(color));
						} else {
							colors.add(ColorRGB.fromBukkitColor(color));
						}
					}
				}
				if (colors.isEmpty())
					return new Color[0];
				return colors.toArray(Color[]::new);
			})
			.build());
	}

	private Literal<Color> colors;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null)
			colors = (Literal<Color>) args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		FireworkExplodeEvent entityEvent = (FireworkExplodeEvent) event;
		if (colors == null)
			return true;

		Set<org.bukkit.Color> colours = Arrays.stream(colors.getAll())
			.map(color -> color instanceof ColorRGB ? color.asBukkitColor() : color.asDyeColor().getFireworkColor())
			.collect(Collectors.toSet());

		FireworkMeta meta = entityEvent.getEntity().getFireworkMeta();
		for (FireworkEffect effect : meta.getEffects()) {
			if (colours.containsAll(effect.getColors()))
				return true;
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("firework exploding")
			.appendIf(colors != null, "colored", colors)
			.toString();
	}

}
