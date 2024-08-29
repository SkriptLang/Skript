package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.jetbrains.annotations.Nullable;

public class EvtSpawner extends SkriptEvent {

	private static final boolean HAS_PRE_SPAWNER_EVENT = Skript.classExists("com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent");

	static {
		Skript.registerEvent("Spawner Spawn", EvtSpawner.class, CollectionUtils.array(com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent.class, SpawnerSpawnEvent.class),"[:pre] spawner spawn[ing] [of %entitydatas%]")
			.description(
				"Called when a spawner spawns an entity.",
				"May also be called before a spawner spawns an entity when specified. This option requires Paper.")
			.examples(
				"on spawner spawn of pig:",
				"\tbroadcast \"A little piggy spawned!\"",
				"",
				"on pre spawner spawn of zombie:",
				"\tbroadcast \"A zombie is about to spawn from a spawner!\"")
			.since("INSERT VERSION");
		EventValues.registerEventValue(PreSpawnerSpawnEvent.class, Block.class, new Getter<>() {
			@Override
			public Block get(PreSpawnerSpawnEvent event) {
				return event.getSpawnerLocation().getBlock();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(SpawnerSpawnEvent.class, Block.class, new Getter<>() {
			@Override
			public Block get(SpawnerSpawnEvent event) {
				return event.getSpawner().getBlock();
			}
		}, EventValues.TIME_NOW);
	}


	private Literal<EntityData<?>> entityTypes;
	private boolean isPre;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		this.entityTypes =  (Literal<EntityData<?>>) args[0];
		isPre = parseResult.hasTag("pre");
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (HAS_PRE_SPAWNER_EVENT && event instanceof PreSpawnerSpawnEvent && isPre) {
			PreSpawnerSpawnEvent spawnerEvent = (PreSpawnerSpawnEvent) event;
			for (EntityData<?> entityType : entityTypes.getArray()) {
				if (EntityUtils.toSkriptEntityData(spawnerEvent.getType()).equals(entityType)) {
					return true;
				}
			}
			return false;
		} else if (event instanceof SpawnerSpawnEvent && !isPre) {
			SpawnerSpawnEvent spawnerEvent = (SpawnerSpawnEvent) event;
			for (EntityData<?> entityType : entityTypes.getArray()) {
				if (EntityUtils.toSkriptEntityData(spawnerEvent.getEntityType()).equals(entityType)) {
					return true;
				}
			}
			return false;
		}
		return false;
	}


	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (isPre ? "pre spawner spawn" : "spawner spawn") + (entityTypes != null ? " of " + entityTypes.toString(event, debug) : "");
	}

}
