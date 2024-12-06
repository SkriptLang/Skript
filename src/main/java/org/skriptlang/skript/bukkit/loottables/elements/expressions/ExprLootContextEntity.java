package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootContextCreateEvent;
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;

@Name("Loot Entity of Loot Context")
@Description("Returns the loot context entity of a loot context.")
@Examples({"set {_entity} to loot context entity of {_context}",
	"",
	"set {_context} to a new loot context at player:",
		"\tset loot context luck value to 10",
		"\tset loot context killer to player",
		"\tset loot context entity to last spawned pig"
})
@Since("INSERT VERSION")
public class ExprLootContextEntity extends SimplePropertyExpression<LootContext, Entity> {

	static {
		registerDefault(ExprLootContextEntity.class, Entity.class, "loot [context] entity", "lootcontexts");
	}

	private boolean isEvent = false;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isEvent = getParser().isCurrentEvent(LootContextCreateEvent.class);
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Entity convert(LootContext context) {
		return context.getLootedEntity();
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!isEvent)
			Skript.error("You can not set the loot context entity of existing loot contexts.");
		else if (mode == ChangeMode.SET)
			return CollectionUtils.array(Entity.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof LootContextCreateEvent createEvent))
			return;

		LootContextWrapper wrapper = createEvent.getContextWrapper();
		wrapper.setEntity((Entity) delta[0]);
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	protected String getPropertyName() {
		return "loot context entity";
	}

}
