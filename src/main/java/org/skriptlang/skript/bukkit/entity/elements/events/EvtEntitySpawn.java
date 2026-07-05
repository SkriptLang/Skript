package org.skriptlang.skript.bukkit.entity.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.log.ErrorQuality;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtEntitySpawn extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtEntitySpawn.class, "Entity Spawn")
			.supplier(EvtEntitySpawn::new)
			.addEvent(EntitySpawnEvent.class)
			.addPatterns("spawn[ing] [of %-entitydatas%]")
			.addDescription("Called when an entity spawns (excluding players).")
			.addExample("""
				on spawn of a zombie:
				    chance of 5%:
				        set scale attribute of event-entity to 3
				        broadcast "A mega zombie has spawned!"
				""")
			.addExample("""
				on spawn of an ender dragon:
				    loop all players in radius 120 of event-entity:
				        send "A great beast has been seen near you.." to loop-value
				""")
			.addSince("1.0, 2.5.1 (non-living entities)")
			.build());
	}

	private @Nullable Literal<EntityData<?>> entityData;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null) {
			entityData = (Literal<EntityData<?>>) args[0];
			for (EntityData<?> value : entityData.getAll()) {
				if (HumanEntity.class.isAssignableFrom(value.getType())) {
					Skript.error("The spawn event does not work for human entities", ErrorQuality.SEMANTIC_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (entityData == null)
			return true;
		EntitySpawnEvent entityEvent = (EntitySpawnEvent) event;
		return entityData.check(event, data -> data.isInstance(entityEvent.getEntity()));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("spawn")
			.appendIf(entityData != null, "of", entityData)
			.toString();
	}

}
