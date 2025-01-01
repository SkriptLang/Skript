package org.skriptlang.skript.bukkit.spawner.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent;
import com.google.common.collect.Lists;
import org.bukkit.event.Event;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.entity.TrialSpawnerSpawnEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitRegistryKeys;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;

import java.util.List;

public class EvtSpawner extends SkriptEvent {

	private static final boolean HAS_PRE = Skript.classExists("com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent");

	static {
		List<Class<? extends Event>> events = Lists.newArrayList(SpawnerSpawnEvent.class, TrialSpawnerSpawnEvent.class);

		if (HAS_PRE)
			events.add(PreSpawnerSpawnEvent.class);

		var info = BukkitSyntaxInfos.Event.builder(EvtSpawner.class, "Spawner Spawn")
			.origin(SyntaxOrigin.of(Skript.instance()))
			.supplier(EvtSpawner::new)
			.priority(SyntaxInfo.COMBINED)
			.addEvents(events)
			.addPattern("[1:pre|2:trial] spawner spawn[ing] [of %-entitydatas%]")
			.addDescription(
				"Called when a spawner spawns an entity.",
				"Use 'trial' to listen for trial spawner spawns.",
				"Use 'pre' to listen for a spawner spawn before it happens.")
			.addExamples(
				"on spawner spawn of pig:",
					"\tbroadcast \"A little piggy spawned!\"",
				"",
				"on trial spawner spawn of chicken:",
					"\tbroadcast \"A chicken spawned from a trial spawner!\"",
				"",
				"on pre spawner spawn of zombie:",
					"\tbroadcast \"A zombie is about to spawn from a spawner!\"")
			.since("INSERT VERSION")
			.addRequiredPlugins(
				"MC 1.21+",
				"Paper (for 'pre' option)")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(BukkitRegistryKeys.EVENT, info);
	}

	private Literal<EntityData<?>> entityTypes;
	private SpawnerEventType eventType;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		//noinspection unchecked
		this.entityTypes =  (Literal<EntityData<?>>) args[0];
		eventType = SpawnerEventType.get(parseResult.mark);

		if (eventType == SpawnerEventType.PRE && !HAS_PRE) {
			Skript.error("The 'pre' option is only available in Paper.");
			return false;
		}

		return true;
	}

	@Override
	public boolean check(Event event) {

		if (entityTypes != null) {
			EntityData<?> type = (eventType == SpawnerEventType.SPAWNER)
				? EntityUtils.toSkriptEntityData(((SpawnerSpawnEvent) event).getEntityType())
				: (eventType == SpawnerEventType.PRE)
					? EntityUtils.toSkriptEntityData(((PreSpawnerSpawnEvent) event).getType())
					: EntityUtils.toSkriptEntityData(((TrialSpawnerSpawnEvent) event).getEntityType());

			for (EntityData<?> entityType : entityTypes.getArray())
				if (type.equals(entityType))
					return true;

			return false;
		}

		return true;
	}


	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		String type = eventType == SpawnerEventType.PRE ? "pre" : (eventType == SpawnerEventType.TRIAL ? "trial" : "");
		builder.append(type)
			.append("spawner spawn")
			.append((entityTypes != null
				? " of " + entityTypes.toString(event, debug)
				: "")
			);

		return builder.toString();
	}

	private enum SpawnerEventType {
		SPAWNER, PRE, TRIAL;

		public static SpawnerEventType get(int mark) {
			return switch (mark) {
				case 1 -> PRE;
				case 2 -> TRIAL;
				default -> SPAWNER;
			};
		}
	}

}
