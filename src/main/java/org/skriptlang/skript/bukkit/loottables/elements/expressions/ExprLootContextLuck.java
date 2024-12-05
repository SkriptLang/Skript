package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
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
public class ExprLootContextLuck extends PropertyExpression<LootContext, Float> {

	static {
		Skript.registerExpression(ExprLootContextLuck.class, Float.class, ExpressionType.PROPERTY,
			"[the] loot [context] luck [value|factor] [of %-lootcontext%]",
			"%lootcontext%'[s] loot [context] luck [value|factor]");
	}

	private boolean isEvent = false;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (exprs[0] == null) {
			if (!getParser().isCurrentEvent(LootContextCreateEvent.class)) {
				Skript.error("There is no loot context in a '" + getParser().getCurrentEventName() + "' event.");
				return false;
			}
			isEvent = true;
		}
		//noinspection unchecked
		setExpr((Expression<LootContext>) exprs[0]);
		return true;
	}


	@Override
	protected Float[] get(Event event, LootContext[] source) {
		return get(source, LootContext::getLuck);
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!isEvent)
			Skript.error("You can not set the loot context luck of existing loot contexts.");
		else
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Float.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof LootContextCreateEvent createEvent))
			return;

		LootContextWrapper wrapper = createEvent.getWrapper();
		float luck = (float) delta[0];
		wrapper.setLuck(luck);
	}

	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (getExpr() == null)
			return "the loot context luck value";
		return "the loot context luck value of " + getExpr().toString(event, debug);
	}

}
