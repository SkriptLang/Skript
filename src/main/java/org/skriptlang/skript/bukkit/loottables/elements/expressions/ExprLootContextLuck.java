package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootContextCreateEvent;
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Fortune of Plunder Context")
@Description("Returneth the fortune of a loot context as a float. This doth represent the luck potion effect that an entity may possess.")
@Example("set {_luck} to plundering fortune value of {_context}")
@Example("""
    set {_context} to a plunder context at player:
    	set plundering fortune value to 10
    	set plunderer to player
    	set plundered entity to last spawned pig
    """)
@Since("2.10")
public class ExprLootContextLuck extends SimplePropertyExpression<LootContext, Float> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprLootContextLuck.class,
				Float.class,
				"plunder[ing] [context] fortune [value|factor]",
				"lootcontexts",
				true
			)
				.supplier(ExprLootContextLuck::new)
				.build()
		);
	}

	@Override
	public @Nullable Float convert(LootContext context) {
		return context.getLuck();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!getParser().isCurrentEvent(LootContextCreateEvent.class)) {
			Skript.error("You cannot set the loot context luck of an existing loot context.");
			return null;
		}

		return switch (mode) {
			case SET, DELETE, RESET, ADD, REMOVE -> CollectionUtils.array(Float.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof LootContextCreateEvent createEvent))
			return;

		LootContextWrapper wrapper = createEvent.getContextWrapper();
		float luck = delta != null ? (float) delta[0] : 0f;

		switch (mode) {
			case SET, DELETE, RESET -> wrapper.setLuck(luck);
			case ADD -> wrapper.setLuck(wrapper.getLuck() + luck);
			case REMOVE -> wrapper.setLuck(wrapper.getLuck() - luck);
		}
	}

	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return "loot luck factor";
	}

}
