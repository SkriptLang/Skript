package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.*;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.Getter;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootContextCreateEvent;
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("New Loot Context")
@Description("Create a loot context.")
@Examples({
	"set {_context} to a new loot context at player:",
		"\tset the loot location of loot context",
	"# this will set {_items::*} to the items that would be dropped from the simple dungeon loot table with the given loot context",
	"",
	"give player loot items of entity's loot table with loot context {_context}",
	"# this will give the player the items that the entity would drop with the given loot context"
})
@Since("INSERT VERSION")
public class ExprSecCreateLootContext extends SectionExpression<LootContext> {

	static {
		Skript.registerExpression(ExprSecCreateLootContext.class, LootContext.class, ExpressionType.SIMPLE,
			"[a] [new] loot context %direction% %location%");
		EventValues.registerEventValue(LootContextCreateEvent.class, LootContextWrapper.class, new Getter<>() {
            @Override
            public @Nullable LootContextWrapper get(LootContextCreateEvent event) {
                return event.getWrapper();
            }
        }, EventValues.TIME_NOW);
	}

	private Trigger trigger;
	private Expression<Location> location;

	@Override
	public boolean init(Expression<?>[] exprs, int pattern, Kleenean isDelayed, SkriptParser.ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		if (node != null) {
			AtomicBoolean delayed = new AtomicBoolean(false);
			Runnable afterLoading = () -> delayed.set(!getParser().getHasDelayBefore().isFalse());
			//noinspection unchecked
			trigger = loadCode(node, "create loot context", afterLoading, LootContextCreateEvent.class);
			if (delayed.get()) {
				Skript.error("Delays cannot be used within a 'create loot context' section.");
				return false;
			}
		}
		//noinspection unchecked
		location = Direction.combine((Expression<Direction>) exprs[0], (Expression<Location>) exprs[1]);
		return true;
	}

	@Override
	protected LootContext @Nullable [] get(Event event) {
		Location loc = location.getSingle(event);
		if (loc == null)
			return new LootContext[0];

		LootContextWrapper wrapper = new LootContextWrapper(loc);
		if (trigger == null)
			return new LootContext[]{wrapper.getContext()};

		LootContextCreateEvent contextEvent = new LootContextCreateEvent(wrapper);

		Variables.setLocalVariables(contextEvent, Variables.copyLocalVariables(event));
		TriggerItem.walk(trigger, contextEvent);
		Variables.setLocalVariables(event, Variables.copyLocalVariables(contextEvent));
		Variables.removeLocals(contextEvent);

		return new LootContext[]{contextEvent.getWrapper().getContext()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends LootContext> getReturnType() {
		return LootContext.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a new loot context at " + location.toString(event, debug);
	}

}
