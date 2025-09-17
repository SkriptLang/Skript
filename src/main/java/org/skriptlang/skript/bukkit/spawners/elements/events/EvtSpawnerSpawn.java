package org.skriptlang.skript.bukkit.spawners.elements.events;

import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.entity.TrialSpawnerSpawnEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitRegistryKeys;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.bukkit.spawners.util.SpawnerDataType;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@SuppressWarnings("UnstableApiUsage")
public class EvtSpawnerSpawn extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		registry.register(BukkitRegistryKeys.EVENT, BukkitSyntaxInfos.Event.builder(EvtSpawnerSpawn.class, "Spawner Spawn")
			.priority(SyntaxInfo.COMBINED)
			.supplier(EvtSpawnerSpawn::new)
			.addEvents(CollectionUtils.array(SpawnerSpawnEvent.class, TrialSpawnerSpawnEvent.class))
			.addPattern("[:trial|:mob] spawner spawn[ing] [of %-entitydatas%]")
			.addDescription("""
				This event is called when a mob spawner or a trial spawner spawns an entity.
				""")
			.addExamples(
				"on trial spawner spawning of a zombie:",
				"on mob spawner spawn:",
				"on spawner spawn of a skeleton:")
			.addSince("INSERT VERSION")
			.build()
		);
	}

	private SpawnerDataType type;
	private Literal<EntityData<?>> entityDatas;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		type = SpawnerDataType.fromTags(parseResult.tags);
		//noinspection unchecked
		entityDatas = (Literal<EntityData<?>>) args[0];
		return true;
	}


	@Override
	public boolean check(Event event) {
		if (entityDatas != null && event instanceof EntityEvent entityEvent) {
			EntityData<?> currentData = EntityUtils.toSkriptEntityData(entityEvent.getEntityType());
			boolean match = entityDatas.stream(event).anyMatch(entityData -> entityData.isSupertypeOf(currentData));

			if (!match)
				return false;
		}

		if (type.isTrial()) {
			return event instanceof TrialSpawnerSpawnEvent;
		} else if (type.isMob()) {
			return event instanceof SpawnerSpawnEvent;
		}

		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append(type.toString(), "spawner spawn");
		if (entityDatas != null)
			builder.append("of", entityDatas);

		return builder.toString();
	}

}
