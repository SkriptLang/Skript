package org.skriptlang.skript.bukkit.spawners.elements.events;

import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitRegistryKeys;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtPreSpawnerSpawn extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		registry.register(BukkitRegistryKeys.EVENT, BukkitSyntaxInfos.Event.builder(EvtPreSpawnerSpawn.class, "Pre Mob Spawner Spawn")
			.priority(SyntaxInfo.COMBINED)
			.supplier(EvtPreSpawnerSpawn::new)
			.addEvent(PreSpawnerSpawnEvent.class)
			.addPatterns("pre spawner spawn [of %-entitydatas%]")
			.addDescription("""
				This event is called right before a mob spawner spawns an entity.
				""")
			.addExamples(
				"on pre spawner spawn:",
				"on pre spawner spawn of a pig:")
			.addSince("INSERT VERSION")
			.build()
		);
	}

	private Literal<EntityData<?>> entityDatas;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		//noinspection unchecked
		entityDatas = (Literal<EntityData<?>>) args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (entityDatas != null && event instanceof PreSpawnerSpawnEvent preEvent) {
			EntityData<?> currentData = EntityUtils.toSkriptEntityData(preEvent.getType());
			return entityDatas.stream(event).anyMatch(entityData -> entityData.isSupertypeOf(currentData));
		}

		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append("pre spawner spawn");
		if (entityDatas != null)
			builder.append("of", entityDatas);

		return builder.toString();
	}

}
