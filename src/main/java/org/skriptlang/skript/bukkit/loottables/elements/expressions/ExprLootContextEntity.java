package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootContextCreateEvent;

@Name("Looted Entity of Loot Context")
@Description("Returns the looted entity of a loot context.")
@Examples({
	"set {_entity} to looted entity of {_context}",
	"",
	"set {_context} to a loot context at player:",
		"\tset loot luck value to 10",
		"\tset looter to player",
		"\tset looted entity to last spawned pig"
})
@Since("INSERT VERSION")
public class ExprLootContextEntity extends SimplePropertyExpression<LootContext, Entity> {

	static {
		registerDefault(ExprLootContextEntity.class, Entity.class, "looted entity", "lootcontexts");
	}

	@Override
	public @Nullable Entity convert(LootContext context) {
		return context.getLootedEntity();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!getParser().isCurrentEvent(LootContextCreateEvent.class)) {
			Skript.error("You cannot set the looted entity of an existing loot context.");
			return null;
		}

		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(Entity.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof LootContextCreateEvent createEvent))
			return;

		Entity entity = delta != null ? (Entity) delta[0] : null;
		createEvent.getContextWrapper().setEntity(entity);
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	protected String getPropertyName() {
		return "looted entity";
	}

}
