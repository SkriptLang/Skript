package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Name("Item Cooldown")
@Description("Change the cooldown of a specific material to a certain amount of <a href='./classes.html#timespan'>Timespan</a>.")
@Examples({
	"on right click using stick:",
		"\tset item cooldown of player's tool for player to 1 minute",
		"\tset item cooldown of stone and grass for all players to 20 seconds",
		"\treset item cooldown of cobblestone and dirt for all players"
})
@RequiredPlugins("MC 1.21.2 (cooldown group)")
@Since({"2.8.0", "INSERT VERSION (cooldown group)"})
public class ExprItemCooldown extends SimpleExpression<Timespan> {

	private static final boolean SUPPORTS_COOLDOWN_GROUP = Skript.methodExists(HumanEntity.class, "getCooldown", ItemStack.class);

	static {
		Skript.registerExpression(ExprItemCooldown.class, Timespan.class, ExpressionType.COMBINED, 
				"[the] [item] cooldown of %itemtypes% for %players%",
				"%players%'[s] [item] cooldown for %itemtypes%");
	}
	
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Player> players;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> itemtypes;
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) exprs[matchedPattern ^ 1];
		itemtypes = (Expression<ItemType>) exprs[matchedPattern];
		return true;
	}
	
	@Override
	protected Timespan[] get(Event event) {
		Player[] players = this.players.getArray(event);
		List<ItemStack> itemStacks = convertToItemList(this.itemtypes.getArray(event));
		List<Timespan> timespans = new ArrayList<>();
		for (Player player : players) {
			for (ItemStack item : itemStacks) {
				if (SUPPORTS_COOLDOWN_GROUP) {
					timespans.add(new Timespan(Timespan.TimePeriod.TICK, player.getCooldown(item)));
					continue;
				}
				timespans.add(new Timespan(Timespan.TimePeriod.TICK, player.getCooldown(item.getType())));
			}
		}
		return timespans.toArray(Timespan[]::new);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return mode == ChangeMode.REMOVE_ALL ? null : CollectionUtils.array(Timespan.class);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (mode != ChangeMode.RESET && mode != ChangeMode.DELETE && delta == null)
			return;
		
		int ticks = delta != null ? (int) ((Timespan) delta[0]).getAs(Timespan.TimePeriod.TICK) : 0; // 0 for DELETE/RESET
		Player[] players = this.players.getArray(event);
		List<ItemStack> itemStacks = convertToItemList(itemtypes.getArray(event));

		for (Player player : players) {
			for (ItemStack itemStack : itemStacks) {
				Material material = itemStack.getType();
				switch (mode) {
					case RESET, DELETE, SET -> {
						if (SUPPORTS_COOLDOWN_GROUP) {
							player.setCooldown(itemStack, ticks);
							break;
						}
						player.setCooldown(material, ticks);
					}
					case REMOVE -> {
						if (SUPPORTS_COOLDOWN_GROUP) {
							player.setCooldown(itemStack, Math.max(player.getCooldown(itemStack) - ticks, 0));
							break;
						}
						player.setCooldown(material, Math.max(player.getCooldown(material) - ticks, 0));
					}
					case ADD -> {
						if (SUPPORTS_COOLDOWN_GROUP) {
							player.setCooldown(itemStack, player.getCooldown(itemStack) + ticks);
							break;
						}
						player.setCooldown(material, player.getCooldown(material) + ticks);
					}
				}
			}
		}
	}

	private List<ItemStack> convertToItemList(ItemType ...itemTypes) {
		return Arrays.stream(itemTypes)
			.filter(ItemType::hasType)
			.map(ItemType::getAll)
			.flatMap(iterator -> {
				List<ItemStack> itemStacks = new ArrayList<>();
				iterator.forEach(itemStacks::add);
				return itemStacks.stream();
			})
			.distinct()
			.toList();
	}

	@Override
	public boolean isSingle() {
		return players.isSingle() && itemtypes.isSingle();
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "cooldown of " + itemtypes.toString(event, debug) + " for " + players.toString(event, debug);
	}

}
