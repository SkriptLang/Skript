package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.loot.LootContext;

@Name("Killer of Loot Context")
@Description("Returns the killer of a loot context.")
@Examples("killer of {_context}")
@Since("INSERT VERSION")
public class ExprLootContextKiller extends SimplePropertyExpression<LootContext, Player> {

	static {
		register(ExprLootContextKiller.class, Player.class, "killer", "lootcontexts");
	}

	@Override
	public Player convert(LootContext context) {
		HumanEntity entity = context.getKiller();
		if (entity instanceof Player player)
			return player;
		return null;
	}

	@Override
	public Class<? extends Player> getReturnType() {
		return Player.class;
	}

	@Override
	protected String getPropertyName() {
		return "killer";
	}
}
