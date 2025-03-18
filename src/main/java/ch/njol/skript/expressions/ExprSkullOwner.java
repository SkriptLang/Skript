package ch.njol.skript.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.bukkitutil.SkullUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.ScriptWarning;

@Name("Skull Owner")
@Description("The skull owner of a player skull.")
@Examples({
	"set {_owner} to the skull owner of event-block",
	"set skull owner of {_block} to \"Njol\" parsed as offlineplayer",
	"set head owner of player's tool to {_player}"
})
@Since("2.9.0, 2.10 (of items)")
public class ExprSkullOwner extends SimplePropertyExpression<Object, OfflinePlayer> {

	static {
		register(ExprSkullOwner.class, OfflinePlayer.class, "(head|skull) owner", "slots/itemtypes/itemstacks/blocks");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		ScriptWarning.printDeprecationWarning("The skull owner expression has been deprecated in favor of the owner of ownable expression.");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable OfflinePlayer convert(Object object) {
		if (object instanceof Block block)
			return SkullUtils.getOwningPlayer(block);

		ItemStack itemStack = ItemUtils.asItemStack(object);
		if (itemStack == null)
			return null;
		return SkullUtils.getOwningPlayer(itemStack);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(OfflinePlayer.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		OfflinePlayer offlinePlayer = (OfflinePlayer) delta[0];
		for (Object object : getExpr().getArray(event)) {
			if (object instanceof Block block) {
				SkullUtils.setOwningPlayer(block, offlinePlayer);
			} else if (object instanceof ItemStack itemStack) {
				SkullUtils.setOwningPlayer(itemStack, offlinePlayer);
			} else if (object instanceof ItemType itemType) {
				SkullUtils.setOwningPlayer(itemType, offlinePlayer);
			} else if (object instanceof Slot slot) {
				ItemStack itemStack = slot.getItem();
				if (itemStack == null)
					continue;
				SkullUtils.setOwningPlayer(itemStack, offlinePlayer);
				slot.setItem(itemStack);
			}
		}
	}

	@Override
	public Class<? extends OfflinePlayer> getReturnType() {
		return OfflinePlayer.class;
	}

	@Override
	protected String getPropertyName() {
		return "skull owner";
	}

}
