package org.skriptlang.skript.bukkit.spawners.elements.sections;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.block.TrialSpawner;
import org.bukkit.event.Event;
import org.bukkit.spawner.Spawner;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawners.util.SpawnerDataType;
import org.skriptlang.skript.bukkit.spawners.util.SpawnerUtils;
import org.skriptlang.skript.bukkit.spawners.util.events.MobSpawnerDataEvent;
import org.skriptlang.skript.bukkit.spawners.util.events.SpawnerDataEvent;
import org.skriptlang.skript.bukkit.spawners.util.events.TrialSpawnerDataEvent;
import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptMobSpawnerData;
import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptTrialSpawnerData;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@Name("Modify Spawner Data")
@Description("""
    Directly modifies the mob, trial, or spawner data of the given spawners. \
    For example, 'modify the ominous and regular trial spawner data of {_trial spawner}' \
    updates both trial spawner's states.
""")
@Example("""
	modify the spawner data of event-block:
		set the activation range to 14
		add 2 to the maximum spawn delay
		delete the spawner entries
	""")
@Example("""
	modify the mob spawner data of event-block:
		set the spawn count to 4
		add 5 to the minimum spawn delay
		add 3 to the maximum nearby entity cap
	""")
@Example("""
	modify the trial spawner data of event-block:
		set the base entity spawn count to 10
		add {_entry} to the spawner entries
		set the reward entry weight for loot table "minecraft:chests/simple_dungeon" to 15
	""")
@Since("INSERT VERSION")
public class SecModifySpawnerData extends Section {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.SECTION, SyntaxInfo.builder(SecModifySpawnerData.class)
			.supplier(SecModifySpawnerData::new)
			.priority(SyntaxInfo.COMBINED)
			.addPatterns(
				"modify [the] [:mob] spawner data of %blocks/entities%",
				"modify [the] [:ominous|:regular|:ominous and regular] trial:trial spawner data of %blocks%")
			.build());
	}

	private enum TrialSpawnerState {
		OMINOUS, REGULAR, BOTH, CURRENT;

		public static TrialSpawnerState fromTags(List<String> tags) {
			if (tags.contains("ominous")) {
				return OMINOUS;
			} else if (tags.contains("ominous and regular")) {
				return BOTH;
			} else if (tags.contains("regular")) {
				return REGULAR;
			} else {
				return CURRENT;
			}
		}
	}


	private Expression<?> spawners;
	private SpawnerDataType dataType;
	private TrialSpawnerState state;

	private Trigger trigger;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
		spawners = exprs[0];
		dataType = SpawnerDataType.fromTags(parseResult.tags);
		state = TrialSpawnerState.fromTags(parseResult.tags);

		trigger = SectionUtils.loadLinkedCode("modify spawner data", (beforeLoading, afterLoading)
			-> loadCode(sectionNode, "modify spawner data", beforeLoading, afterLoading, SpawnerDataEvent.class));
		return trigger != null;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		for (Object spawnerObject : spawners.getArray(event)) {
			if (!dataType.matches(spawnerObject))
				continue;

			if (SpawnerUtils.isMobSpawner(spawnerObject)) {
				Spawner mobSpawner = SpawnerUtils.getMobSpawner(spawnerObject);
				SkriptMobSpawnerData mobData = SkriptMobSpawnerData.fromSpawner(mobSpawner);

				MobSpawnerDataEvent mobEvent = new MobSpawnerDataEvent(mobData);
				Variables.withLocalVariables(event, mobEvent, () -> TriggerItem.walk(trigger, mobEvent));

				mobData.applyData(mobSpawner);
			} else {
				TrialSpawner trialSpawner = SpawnerUtils.getTrialSpawner(spawnerObject);
				SkriptTrialSpawnerData trialData = switch (state) {
					case CURRENT -> SkriptTrialSpawnerData.fromTrialSpawner(trialSpawner, trialSpawner.isOminous());
					case OMINOUS -> SkriptTrialSpawnerData.fromTrialSpawner(trialSpawner, true);
					// start with regular, then modify the ominous if needed
					case REGULAR, BOTH -> SkriptTrialSpawnerData.fromTrialSpawner(trialSpawner, false);
				};

				TrialSpawnerDataEvent regularEvent = new TrialSpawnerDataEvent(trialData);
				Variables.withLocalVariables(event, regularEvent, () -> TriggerItem.walk(trigger, regularEvent));

				if (state == TrialSpawnerState.BOTH) {
					// guaranteed to be the regular data here
					trialData.applyData(trialSpawner, false);

					// modify the ominous data
					trialData = SkriptTrialSpawnerData.fromTrialSpawner(trialSpawner, true);
					TrialSpawnerDataEvent ominousEvent = new TrialSpawnerDataEvent(trialData);
					Variables.withLocalVariables(event, ominousEvent, () -> TriggerItem.walk(trigger, ominousEvent));
					trialData.applyData(trialSpawner, true);
				} else {
					trialData.applyData(trialSpawner, switch (state) {
						case CURRENT -> trialSpawner.isOminous();
						case OMINOUS -> true;
						case REGULAR -> false;
						case BOTH -> throw new IllegalStateException();
					});
				}
			}
		}

		return super.walk(event, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append("modify the");
		if (dataType.isTrial()) {
			if (state == TrialSpawnerState.REGULAR) {
				builder.append("regular");
			} else if (state == TrialSpawnerState.OMINOUS) {
				builder.append("ominous");
			} else {
				builder.append("ominous and regular");
			}
		}
		builder.append(dataType.toString(), "spawner data of", spawners);

		return builder.toString();
	}

}
