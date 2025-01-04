package org.skriptlang.skript.bukkit.spawner.elements.expressions.spawnerentry;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.events.SpawnerEntryCreateEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

public class ExprSecSpawnerEntry extends SectionExpression<SpawnerEntry> {

	static {
		var info = SyntaxInfo.Expression.builder(ExprSecSpawnerEntry.class, SpawnerEntry.class)
			.origin(SyntaxOrigin.of(Skript.instance()))
			.supplier(ExprSecSpawnerEntry::new)
			.priority(SyntaxInfo.COMBINED)
			.addPattern("[a] spawner entry (using|with) %entitysnapshot%")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.EXPRESSION, info);

		EventValues.registerEventValue(SpawnerEntryCreateEvent.class, SpawnerEntry.class, SpawnerEntryCreateEvent::getSpawnerEntry);
	}

	private Trigger trigger;
	private Expression<EntitySnapshot> snapshot;

	@Override
	public boolean init(Expression<?>[] exprs, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node,
	                    @Nullable List<TriggerItem> triggerItems) {
		if (node != null)
			//noinspection unchecked
			trigger = loadCode(node, "create spawner entry", null, SpawnerEntryCreateEvent.class);
		//noinspection unchecked
		snapshot = (Expression<EntitySnapshot>) exprs[0];
		return true;
	}

	@Override
	protected SpawnerEntry @Nullable [] get(Event event) {
		EntitySnapshot snapshot = this.snapshot.getSingle(event);
		if (snapshot == null)
			return new SpawnerEntry[0];

		SpawnerEntry entry = new SpawnerEntry(snapshot, 1, null);
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
		return "spawner entry using " + snapshot.toString(event, debug);
	}

}
