package org.skriptlang.skript.bukkit.spawner.elements.expressions.spawnerentry;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.*;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.events.SpawnerEntryCreateEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@Name("Create Spawner Entry")
@Description({
	"Creates a spawner entry from the given entity snapshot. "
		+ "Spawner entries are used to modify what type of entity the spawner will spawn, "
		+ "with what equipment, rules, etc."
})
@Examples({
	"set {_entry} to a spawner entry using entity snapshot of a zombie:",
		"\tset the weight to 5",
		"\tset the spawn rule to a spawn rule:",
			"\t\tset the minimum block light spawn level to 10",
			"\t\tset the maximum block light spawn level to 15",
			"\t\tset the maximum sky light spawn level to 15",
	"add {_entry} to potential spawns of target block",
	"",
	"set {_entry} to a spawner entry with event-entity:",
		"\tset the weight to 10",
		"\tset the spawn rule to a spawn rule:",
			"\t\tset the minimum block light spawn level to 12",
			"\t\tset the maximum block light spawn level to 12",
			"\t\tset the maximum sky light spawn level to 5",
	"add {_entry} to potential spawns of target block"
})
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.21+")
public class ExprSecSpawnerEntry extends SectionExpression<SpawnerEntry> {

	static {
		var info = SyntaxInfo.Expression.builder(ExprSecSpawnerEntry.class, SpawnerEntry.class)
			.origin(SyntaxOrigin.of(Skript.instance()))
			.supplier(ExprSecSpawnerEntry::new)
			.priority(SyntaxInfo.COMBINED)
			.addPattern("[a] spawner entry (using|with) %entitysnapshot/entity/entitydata%")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.EXPRESSION, info);

		EventValues.registerEventValue(SpawnerEntryCreateEvent.class, SpawnerEntry.class, SpawnerEntryCreateEvent::getSpawnerEntry);
	}

	private Trigger trigger;
	private Expression<?> object;

	@Override
	public boolean init(Expression<?>[] exprs, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node,
	                    @Nullable List<TriggerItem> triggerItems) {
		if (node != null)
			//noinspection unchecked
			trigger = loadCode(node, "create spawner entry", null, SpawnerEntryCreateEvent.class);
		object = exprs[0];
		return true;
	}

	@Override
	protected SpawnerEntry @Nullable [] get(Event event) {
		Object object = this.object.getSingle(event);
		if (object == null)
			return new SpawnerEntry[0];

		EntitySnapshot entitySnapshot;
		if (object instanceof EntitySnapshot snapshot) {
			entitySnapshot = snapshot;
		} else if (object instanceof Entity entity) {
			entitySnapshot = entity.createSnapshot();
		} else if (object instanceof EntityData<?> data) {
			Entity entity = data.create();
			if (entity == null)
				return new SpawnerEntry[0];
			entitySnapshot = entity.createSnapshot();
		} else {
			return new SpawnerEntry[0];
		}

		if (entitySnapshot == null)
			return new SpawnerEntry[0];

		SpawnerEntry entry = new SpawnerEntry(entitySnapshot, 1, null);
		if (trigger != null) {
			SpawnerEntryCreateEvent createEvent = new SpawnerEntryCreateEvent(entry);
			Variables.withLocalVariables(event, createEvent, () ->
					TriggerItem.walk(trigger, createEvent)
			);
		}

		return new SpawnerEntry[]{entry};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends SpawnerEntry> getReturnType() {
		return SpawnerEntry.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "spawner entry using " + object.toString(event, debug);
	}

}
