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
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootContextCreateEvent;
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;

@Name("Killer of Loot Context")
@Description("Returns the loot context killer of a loot context.")
@Examples({
	"set {_killer} to loot context killer of {_context}",
	"",
	"set {_context} to a new loot context at player:",
		"\tset loot context luck value to 10",
		"\tset loot context killer to player",
		"\tset loot context entity to last spawned pig"
})
@Since("INSERT VERSION")
public class ExprLootContextKiller extends SimplePropertyExpression<LootContext, Player> {

	static {
		registerDefault(ExprLootContextKiller.class, Player.class, "loot [context] killer", "lootcontexts");
	}

	private boolean isEvent;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isEvent = getParser().isCurrentEvent(LootContextCreateEvent.class);
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Player convert(LootContext context) {
		if (context.getKiller() instanceof Player player)
			return player;
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!isEvent)
			Skript.error("You can not set the loot context killer of existing loot contexts.");
		else if (mode == ChangeMode.SET)
			return CollectionUtils.array(Player.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof LootContextCreateEvent createEvent))
			return;

		LootContextWrapper wrapper = createEvent.getContextWrapper();
		wrapper.setKiller((Player) delta[0]);
	}

	@Override
	public Class<? extends Player> getReturnType() {
		return Player.class;
	}

	@Override
	protected String getPropertyName() {
		return "killer of loot context";
	}

}
