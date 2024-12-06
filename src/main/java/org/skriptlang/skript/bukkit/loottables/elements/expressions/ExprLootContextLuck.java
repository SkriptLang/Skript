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
import org.bukkit.event.Event;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootContextCreateEvent;
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;

@Name("Luck of Loot Context")
@Description("Returns the number of luck of a loot context.")
@Examples({"set {_luck} to loot context luck value of {_context}",
	"",
	"set {_context} to a new loot context at player:",
		"\tset loot context luck value to 10",
		"\tset loot context killer to player",
		"\tset loot context entity to last spawned pig"
})
@Since("INSERT VERSION")
public class ExprLootContextLuck extends SimplePropertyExpression<LootContext, Float> {

	static {
		registerDefault(ExprLootContextLuck.class, Float.class, "loot [context] luck [value|factor]", "lootcontexts");
	}

	private boolean isEvent = false;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isEvent = getParser().isCurrentEvent(LootContextCreateEvent.class);
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Float convert(LootContext context) {
		return context.getLuck();
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!isEvent)
			Skript.error("You can not set the loot context luck of existing loot contexts.");
		else if (mode == ChangeMode.SET)
			return CollectionUtils.array(Float.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof LootContextCreateEvent createEvent))
			return;

		LootContextWrapper wrapper = createEvent.getContextWrapper();
		float luck = (float) delta[0];
		wrapper.setLuck(luck);
	}

	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return null;
	}

}
