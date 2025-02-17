package ch.njol.skript.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

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
	public @Nullable OfflinePlayer convert(Object object) {
		if (object instanceof Block block && block.getState() instanceof Skull skull) {
			return skull.getOwningPlayer();
		} else {
			ItemStack skullItem = ItemUtils.asItemStack(object);
			if (skullItem == null || !(skullItem.getItemMeta() instanceof SkullMeta skullMeta))
				return null;
			return skullMeta.getOwningPlayer();
		}
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
		Consumer<Skull> blockChanger = getBlockChanger(offlinePlayer);
		Consumer<SkullMeta> metaChanger = getMetaChanger(offlinePlayer);
		for (Object object : getExpr().getArray(event)) {
			if (object instanceof Block block && block.getState() instanceof Skull skull) {
				blockChanger.accept(skull);
				skull.update(true, false);
			} else {
				ItemStack skullItem = ItemUtils.asItemStack(object);
				if (skullItem == null || !(skullItem.getItemMeta() instanceof SkullMeta skullMeta))
					continue;
				metaChanger.accept(skullMeta);
				if (object instanceof Slot slot) {
					skullItem.setItemMeta(skullMeta);
					slot.setItem(skullItem);
				} else if (object instanceof ItemType itemType) {
					itemType.setItemMeta(skullMeta);
				} else if (object instanceof ItemStack itemStack) {
					itemStack.setItemMeta(skullMeta);
				}
			}
		}
	}

	private Consumer<Skull> getBlockChanger(OfflinePlayer offlinePlayer) {
		if (offlinePlayer.getName() != null) {
			return skull -> skull.setOwningPlayer(offlinePlayer);
		} else if (ItemUtils.CAN_CREATE_PLAYER_PROFILE) {
			//noinspection deprecation
			PlayerProfile profile = Bukkit.createPlayerProfile(offlinePlayer.getUniqueId(), "");
			//noinspection deprecation
			return skull -> skull.setOwnerProfile(profile);
		}
		//noinspection deprecation
		return skull -> skull.setOwner("");
	}

	private Consumer<SkullMeta> getMetaChanger(OfflinePlayer offlinePlayer) {
		if (offlinePlayer.getName() != null) {
			return skullMeta -> skullMeta.setOwningPlayer(offlinePlayer);
		} else if (ItemUtils.CAN_CREATE_PLAYER_PROFILE) {
			//noinspection deprecation
			PlayerProfile profile = Bukkit.createPlayerProfile(offlinePlayer.getUniqueId(), "");
			//noinspection deprecation
			return skullMeta -> skullMeta.setOwnerProfile(profile);
		}
		//noinspection deprecation
		return skullMeta -> skullMeta.setOwner("");
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
