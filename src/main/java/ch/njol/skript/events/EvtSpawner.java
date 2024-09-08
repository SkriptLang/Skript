package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent;
import com.google.common.collect.Lists;
import org.bukkit.event.Event;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EvtSpawner extends SkriptEvent {

	private static final boolean HAS_PRE_SPAWNER_EVENT = Skript.classExists("com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent");

	static {
		List<Class<? extends Event>> events = Lists.newArrayList(SpawnerSpawnEvent.class);
		if (HAS_PRE_SPAWNER_EVENT) {
			events.add(PreSpawnerSpawnEvent.class);
		}
		Skript.registerEvent("Spawner Spawn", EvtSpawner.class, events.toArray(new Class[0]),"[:pre] spawner spawn[ing] [of %-entitydatas%]")
			.description(
				"Called when a spawner spawns an entity.",
				"May also be called before a spawner spawns an entity when specified. This option requires a compatible Paper version.")
			.examples(
				"on spawner spawn of pig:",
					"\tbroadcast \"A little piggy spawned!\"",
				"",
				"on pre spawner spawn of zombie:",
					"\tbroadcast \"A zombie is about to spawn from a spawner!\"")
			.since("INSERT VERSION");

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
