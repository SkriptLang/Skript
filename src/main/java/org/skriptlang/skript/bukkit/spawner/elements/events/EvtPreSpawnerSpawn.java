package org.skriptlang.skript.bukkit.spawner.elements.events;

import ch.njol.skript.Skript;
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
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;

public class EvtPreSpawnerSpawn extends SkriptEvent {

	private static final boolean HAS_PRE = Skript.classExists("com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent");

	static {
		if (HAS_PRE) {
			var info = BukkitSyntaxInfos.Event.builder(EvtPreSpawnerSpawn.class, "Pre Spawner Spawn")
				.origin(SyntaxOrigin.of(Skript.instance()))
				.supplier(EvtPreSpawnerSpawn::new)
				.priority(SyntaxInfo.COMBINED)
				.addEvent(PreSpawnerSpawnEvent.class)
				.addPattern("pre spawner spawn [of %-entitydatas%]")
				.addDescription("Called when a spawner is about to spawn an entity.")
				.addExamples(
					"on pre spawner spawn of pig:",
						"\tbroadcast \"A little piggy is about to spawn!\"")
				.since("INSERT VERSION")
				.addRequiredPlugin("Paper 1.21+")
				.build();

			SpawnerModule.SYNTAX_REGISTRY.register(BukkitRegistryKeys.EVENT, info);
		}

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
		if (entityDatas != null) {
			EntityData<?> type = EntityUtils.toSkriptEntityData(((PreSpawnerSpawnEvent) event).getType());
			for (EntityData<?> entityType : entityDatas.getArray())
				if (type.equals(entityType))
					return true;

			return false;
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
