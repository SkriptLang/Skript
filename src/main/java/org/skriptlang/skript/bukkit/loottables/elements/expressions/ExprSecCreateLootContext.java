package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Direction;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootContextCreateEvent;
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@Name("Create Loot Context")
@Description("Create a loot context.")
@Example("""
    set {_player} to player
    set {_context} to a loot context at player:
        set loot luck value to 10
        set looter to {_player}
        set looted entity to last spawned pig
    give player loot items of loot table "minecraft:entities/iron_golem" with loot context {_context}
    """)
@Since("2.10")
public class ExprSecCreateLootContext extends SectionExpression<LootContext> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(ExprSecCreateLootContext.class, LootContext.class)
				.addPatterns("[a] loot context %direction% %location%")
				.supplier(ExprSecCreateLootContext::new)
				.build()
		);
		EventValues.registerEventValue(LootContextCreateEvent.class, LootContext.class, event -> event.getContextWrapper().getContext());
	}

	private Trigger trigger;
	private Expression<Location> location;

	@Override
	public boolean init(Expression<?>[] exprs, int pattern, Kleenean isDelayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		//noinspection unchecked
		location = Direction.combine((Expression<Direction>) exprs[0], (Expression<Location>) exprs[1]);

		if (node != null) {
			//noinspection unchecked
			trigger = SectionUtils.loadLinkedCode("create loot context", (beforeLoading, afterLoading)
					-> loadCode(node, "create loot context", beforeLoading, afterLoading, LootContextCreateEvent.class));
			return trigger != null;
		}

		return true;
	}

	@Override
	protected LootContext @Nullable [] get(Event event) {
		Location loc = location.getSingle(event);
		if (loc == null)
			return new LootContext[0];

		LootContextWrapper wrapper = new LootContextWrapper(loc);
		if (trigger != null) {
			LootContextCreateEvent contextEvent = new LootContextCreateEvent(wrapper);
			Variables.withLocalVariables(event, contextEvent, () ->
				TriggerItem.walk(trigger, contextEvent)
			);
		}
		return new LootContext[]{wrapper.getContext()};
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
		return "a loot context " + location.toString(event, debug);
	}

}
