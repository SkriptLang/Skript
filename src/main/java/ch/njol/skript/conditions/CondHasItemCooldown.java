package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("Hath Item Cooldown")
@Description("""
    Checketh whether a cooldown be active upon the specified item for a particular player.
    Should the provided item bear a cooldown group component, the cooldown group shall take precedence.
    Otherwise, the cooldown of the item's material shall be employed.
    """)
@Example("""
    if player has player's tool upon cooldown:
    	send "Thou canst not wield this item presently. Tarry %item cooldown of player's tool for player%"
    """)
@RequiredPlugins("MC 1.21.2 (cooldown group)")
@Since({"2.8.0", "2.12 (cooldown group)"})
public class CondHasItemCooldown extends Condition {

	// Cooldown groups were added in Minecraft 1.21.2
	// a link to the data component can be found here https://minecraft.wiki/w/Data_component_format#use_cooldown
	// The cooldown is applied to the material if no cooldown group is defined on the provided itemstack.
	private static final boolean SUPPORTS_COOLDOWN_GROUP = Skript.methodExists(HumanEntity.class, "hasCooldown", ItemStack.class);

	static {
		Skript.registerCondition(CondHasItemCooldown.class, 
				"%players% (has|have) [([an] item|a)] cooldown (on|for) %itemtypes%",
				"%players% (has|have) %itemtypes% upon [(item|a)] cooldown",
				"%players% (doesn't|does not|do not|don't) have [([an] item|a)] cooldown (on|for) %itemtypes%",
				"%players% (doesn't|does not|do not|don't) have %itemtypes% upon [(item|a)] cooldown");
	}

	private Expression<Player> players;
	private Expression<ItemType> itemTypes;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		itemTypes = (Expression<ItemType>) exprs[1];
		setNegated(matchedPattern > 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		ItemType[] itemTypes = this.itemTypes.getArray(event);
		return players.check(event, (player) -> {
			return SimpleExpression.check(itemTypes, itemType -> {
				if (!itemType.hasType())
					return false;
				if (SUPPORTS_COOLDOWN_GROUP)
					return itemType.satisfies(player::hasCooldown);
				return itemType.satisfies(item -> player.hasCooldown(item.getType()));
			}, false, this.itemTypes.getAnd());
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.HAVE, event, debug, players,
				itemTypes.toString(event, debug) + " on cooldown");
	}

}
