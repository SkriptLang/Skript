package org.skriptlang.skript.bukkit.block.elements.events;

import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.event.Event;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtBeaconEffect extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtBeaconEffect.class, "Beacon Effect")
			.supplier(EvtBeaconEffect::new)
			.addEvent(BeaconEffectEvent.class)
			.addPatterns(
				"[:primary|:secondary] beacon effect [of %-potioneffecttypes%]",
				"application of [:primary|:secondary] beacon effect [of %-potioneffecttypes%]",
				"[:primary|:secondary] beacon effect apply [of %-potioneffecttypes%]"
			)
			.addDescription("Called when a player gets an effect from a beacon.")
			.addExample("""
				on beacon effect:
					broadcast "%event-player% just got %applied effect% beacon effect at the beacon %event-block%!"
					broadcast
				""")
			.addExample("""
				on primary beacon effect apply of haste:
					broadcast "I could mine through a mountain.."
				""")
			.addExample("""
					on application of secondary beacon effect:
						broadcast "secondary exposure!"
				""")
			.addExample("""
				on beacon effect of speed:
					broadcast "Feeling speedy today!"
				""")
			.addSince("2.10")
			.build());

		eventValueRegistry.register(EventValue.builder(BeaconEffectEvent.class, PotionEffectType.class)
			.getter(event -> event.getEffect().getType())
			.excludes(BeaconEffectEvent.class)
			.excludedErrorMessage("Use 'applied effect' in beacon effect events.")
			.build());

		eventValueRegistry.register(EventValue.builder(BeaconEffectEvent.class, Player.class)
			.getter(BeaconEffectEvent::getPlayer)
			.build());
	}

	private @Nullable Literal<PotionEffectType> potionTypes;
	private @Nullable Boolean primaryCheck;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		potionTypes = (Literal<PotionEffectType>) args[0];
		if (parseResult.hasTag("primary")) {
			primaryCheck = true;
		} else if (parseResult.hasTag("secondary")) {
			primaryCheck = false;
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		BeaconEffectEvent effectEvent = (BeaconEffectEvent) event;
		if (primaryCheck != null && effectEvent.isPrimary() != primaryCheck)
			return false;
		if (potionTypes != null)
			return potionTypes.check(event, type -> effectEvent.getEffect().getType() == type);
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.appendIf(primaryCheck != null, primaryCheck ? "primary " : "secondary ")
			.append("beacon effect")
			.appendIf(potionTypes != null, potionTypes)
			.toString();
	}

}
