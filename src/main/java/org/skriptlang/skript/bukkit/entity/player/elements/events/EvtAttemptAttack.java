package org.skriptlang.skript.bukkit.entity.player.elements.events;

import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.LiteralList;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.event.Event;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtAttemptAttack extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtAttemptAttack.class, "Attempt Attack")
			.supplier(EvtAttemptAttack::new)
			.addEvent(PrePlayerAttackEntityEvent.class)
			.addPatterns("attack attempt", "attempt[ing] to attack %entitydatas%")
			.addDescription("""
				Called when a player attempts to attack an entity.
				The event will be cancelled as soon as it is fired for non-living entities.
				Cancelling this event will prevent the attack and any sounds from being played when attacking.
				Any damage events will not be called if this is cancelled.
				""")
			.addExample("""
				on attack attempt:
					if event is cancelled:
						broadcast "%attacker% failed to attack %victim%!"
					else:
						broadcast "%attacker% damaged %victim%!"
				""")
			.addExample("""
				on attempt to attack an animal:
					cancel event
				""")
			.addExample("""
				on attempt to attack a zombie or creeper:
					attacker isn't holding a diamond sword
					cancel event
				""")
			.addSince("2.15")
			.build());
	}

	private @Nullable Literal<EntityData<?>> entityData;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (matchedPattern == 1) {
			entityData = (Literal<EntityData<?>>) args[0];
			if (entityData.getAnd() && entityData instanceof LiteralList<EntityData<?>> list)
				list.invertAnd();
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (entityData == null)
			return true;

		PrePlayerAttackEntityEvent playerEvent = (PrePlayerAttackEntityEvent) event;

		return entityData.check(event, data -> data.isInstance(playerEvent.getAttacked()));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return entityData != null ? "attempting to attack" + entityData.toString(event,debug) : "attack attempt";
	}

}
