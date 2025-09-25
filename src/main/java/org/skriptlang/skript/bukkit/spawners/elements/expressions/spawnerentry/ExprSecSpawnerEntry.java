package org.skriptlang.skript.bukkit.spawners.elements.expressions.spawnerentry;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawners.util.SkriptSpawnerEntry;
import org.skriptlang.skript.bukkit.spawners.util.events.SpawnRuleEvent;
import org.skriptlang.skript.bukkit.spawners.util.events.SpawnerEntryEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@Name("New Spawner Entry")
@Description("Returns a new spawner entry from the given entity snapshot or data.")
@Example("""
	set {_entry} to the spawner entry of a zombie:
		set the weight to 5
		set the spawn rule to a spawn rule:
			set the maximum block light spawn level to 15
			set the minimum block light spawn level to 10
			set the maximum sky light spawn level to 15

		set the spawner entry equipment to loot table "minecraft:equipment/trial_chamber"
		set the drop chances for helmet, legs and boots to 100%
	add {_entry} to the spawner entries of {_data}
	""")
@Since("INSERT VERSION")
public class ExprSecSpawnerEntry extends SectionExpression<SkriptSpawnerEntry> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprSecSpawnerEntry.class, SkriptSpawnerEntry.class)
			.supplier(ExprSecSpawnerEntry::new)
			.priority(SyntaxInfo.COMBINED)
			.addPattern("[a|the] spawner entry (of|using) %entitydata/entitysnapshot%")
			.build()
		);
	}

	private Trigger trigger;
	private Expression<?> entity;

	@Override
	public boolean init(
		Expression<?>[] exprs, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node,
		@Nullable List<TriggerItem> triggerItems
	) {
		entity = exprs[0];
		if (node != null) {
			trigger = SectionUtils.loadLinkedCode("spawner entry create", (beforeLoading, afterLoading) ->
				loadCode(node, "spawner entry create", beforeLoading, afterLoading, SpawnRuleEvent.class)
			);
			return trigger != null;
		}
		return true;
	}

	@Override
	protected SkriptSpawnerEntry @Nullable [] get(Event event) {
		Object object = entity.getSingle(event);
		if (object == null)
			return null;

		EntitySnapshot snapshot = null;

		if (object instanceof EntityData<?> entityData) {
			Entity entity = entityData.create();
			if (entity == null)
				return null;

			//noinspection UnstableApiUsage
			snapshot = entity.createSnapshot();
			if (snapshot == null)
				return null;

			entity.remove();
		} else if (object instanceof EntitySnapshot entitySnapshot) {
			snapshot = entitySnapshot;
		}

		assert snapshot != null;

		SkriptSpawnerEntry entry = new SkriptSpawnerEntry(snapshot);
		if (trigger != null) {
			SpawnerEntryEvent entryEvent = new SpawnerEntryEvent(entry);
			Variables.withLocalVariables(event, entryEvent, () ->
					TriggerItem.walk(trigger, entryEvent)
			);
		}

		return new SkriptSpawnerEntry[]{entry};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends SkriptSpawnerEntry> getReturnType() {
		return SkriptSpawnerEntry.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the spawner entry of " + entity.toString(event, debug);
	}

}
