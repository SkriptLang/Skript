package org.skriptlang.skript.bukkit.spawners.elements.expressions.spawner.spawnerdata;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawners.util.SpawnerDataType;
import org.skriptlang.skript.bukkit.spawners.util.events.MobSpawnerDataEvent;
import org.skriptlang.skript.bukkit.spawners.util.events.TrialSpawnerDataEvent;
import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptMobSpawnerData;
import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptSpawnerData;
import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptTrialSpawnerData;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@Name("New Spawner Data")
@Description("Returns a new trial or mob spawner data.")
@Example("""
	set spawner data of event-block to the mob spawner data:
		set the spawn count to 2
		add 2 to the maximum nearby entity cap
		remove 5 from the activation range
		add {_entry} to the spawner entries
		set the spawn range to 16
	""")
@Example("""
	set {_trial data} to the trial spawner data:
		set the activation range to 32
		set the spawn range to 8
		add {_entries::*} to the spawner entries
		set the base entity spawn count to 12

		add loot table "minecraft:chests/simple_dungeon" to the reward entries
		set the reward weight for loot table "minecraft:chests/simple_dungeon" to 12

	set the ominous trial spawner data of event-block to {_trial data}
	""")
@Since("INSERT VERSION")
public class ExprSecSpawnerData extends SectionExpression<SkriptSpawnerData> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprSecSpawnerData.class, SkriptSpawnerData.class)
			.supplier(ExprSecSpawnerData::new)
			.priority(SyntaxInfo.SIMPLE)
			.addPatterns("[the] mob spawner data", "[the] trial spawner data")
			.build());
	}

	private SpawnerDataType dataType;
	private Trigger trigger;

	@Override
	public boolean init(
		Expression<?>[] exprs, int pattern, Kleenean delayed, ParseResult result,
		@Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems
	) {
		dataType = pattern == 0 ? SpawnerDataType.MOB : SpawnerDataType.TRIAL;
		if (node != null) {
			String name = dataType + " spawner data";
			trigger = SectionUtils.loadLinkedCode(name, (beforeLoading, afterLoading) ->
				loadCode(node, name, beforeLoading, afterLoading, MobSpawnerDataEvent.class));
			return trigger != null;
		}
		return true;
	}

	@Override
	protected SkriptSpawnerData @Nullable [] get(Event event) {
		SkriptSpawnerData data = (dataType.isMob())
			? new SkriptMobSpawnerData()
			: new SkriptTrialSpawnerData();

		if (trigger != null) {
			//noinspection DataFlowIssue
			Event dataEvent = (dataType.isMob())
				? new MobSpawnerDataEvent((SkriptMobSpawnerData) data)
				: new TrialSpawnerDataEvent((SkriptTrialSpawnerData) data);

			Variables.withLocalVariables(event, dataEvent, () ->
				TriggerItem.walk(trigger, dataEvent)
			);
		}
		return new SkriptSpawnerData[]{data};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends SkriptSpawnerData> getReturnType() {
		return dataType.getDataClass();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return dataType + " spawner data";
	}

}
