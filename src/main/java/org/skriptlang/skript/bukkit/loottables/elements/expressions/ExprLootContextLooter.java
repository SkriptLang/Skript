package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootContextCreateEvent;

@Name("Looter of Loot Context")
@Description(
	"Returns the looter of a loot context. "
		+ "Note that setting the looter will read the looter's tool enchantments (e.g. looting) when generating loot."
)
@Examples({
	"set {_killer} to looter of {_context}",
	"",
	"set {_context} to a loot context at player:",
		"\tset loot luck value to 10",
		"\tset looter to player",
		"\tset looted entity to last spawned pig"
})
@Since("INSERT VERSION")
public class ExprLootContextLooter extends SimplePropertyExpression<LootContext, Player> {

	static {
		registerDefault(ExprLootContextLooter.class, Player.class, "(looter|looting player)", "lootcontexts");
	}

	@Override
	public @Nullable Player convert(LootContext context) {
		if (context.getKiller() instanceof Player player)
			return player;
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!getParser().isCurrentEvent(LootContextCreateEvent.class)) {
			Skript.error("You cannot set the looting player of an existing loot context.");
			return null;
		}

		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(Player.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof LootContextCreateEvent createEvent))
			return;

		Player player = delta != null ? (Player) delta[0] : null;
		createEvent.getContextWrapper().setKiller(player);
	}

	@Override
	public Class<? extends Player> getReturnType() {
		return Player.class;
	}

	@Override
	protected String getPropertyName() {
		return "looting player";
	}

}
