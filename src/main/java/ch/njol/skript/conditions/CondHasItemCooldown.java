package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Has Item Cooldown")
@Description("Check whether a cooldown is active on the specified material for a specific player.")
@Examples({
	"if player has player's tool on cooldown:",
		"\tsend \"You can't use this item right now. Wait %item cooldown of player's tool for player%\""
})
@RequiredPlugins("MC 1.21.2 (cooldown group)")
@Since({"2.8.0", "INSERT VERSION (cooldown group)"})
public class CondHasItemCooldown extends Condition {

	private static final boolean SUPPORTS_COOLDOWN_GROUP = Skript.methodExists(HumanEntity.class, "getCooldown", ItemStack.class);

	static {
		Skript.registerCondition(CondHasItemCooldown.class, 
				"%players% (has|have) [([an] item|a)] cooldown (on|for) %itemtypes%",
				"%players% (has|have) %itemtypes% on [(item|a)] cooldown",
				"%players% (doesn't|does not|do not|don't) have [([an] item|a)] cooldown (on|for) %itemtypes%",
				"%players% (doesn't|does not|do not|don't) have %itemtypes% on [(item|a)] cooldown");
	}

	private Expression<Player> players;
	private Expression<ItemType> itemtypes;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		itemtypes = (Expression<ItemType>) exprs[1];
		setNegated(matchedPattern > 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return players.check(event, (player) ->
				itemtypes.check(event, (itemType) -> {
					if (!itemType.hasType())
						return false;
					for (ItemStack item : itemType.getAll()) {
						if (SUPPORTS_COOLDOWN_GROUP && player.hasCooldown(item)) {
							return true;
						} else if (player.hasCooldown(item.getType())) {
							return true;
						}
					}
					return false;
				})
		);
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.HAVE, event, debug, players,
				itemtypes.toString(event, debug) + " on cooldown");
	}

}
