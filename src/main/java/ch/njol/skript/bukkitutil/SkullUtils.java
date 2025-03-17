package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SkullUtils {

	public static final boolean CAN_CREATE_PLAYER_PROFILE = Skript.methodExists(Bukkit.class, "createPlayerProfile", UUID.class, String.class);
	/** Paper does not do texture lookups by default */
	public static final boolean REQUIRES_TEXTURE_LOOKUP = Skript.classExists("com.destroystokyo.paper.profile.PlayerProfile") && Skript.isRunningMinecraft(1, 19, 4);

	public static @Nullable OfflinePlayer getOwningPlayer(ItemType itemType) {
		if (!(itemType.getItemMeta() instanceof SkullMeta skullMeta))
			return null;
		return skullMeta.getOwningPlayer();
	}

	public static @Nullable OfflinePlayer getOwningPlayer(ItemStack itemStack) {
		if (!itemStack.hasItemMeta() || !(itemStack.getItemMeta() instanceof SkullMeta skullMeta))
			return null;
		return skullMeta.getOwningPlayer();
	}

	public static @Nullable OfflinePlayer getOwningPlayer(Block block) {
		if (!(block.getState() instanceof Skull skull))
			return null;
		return skull.getOwningPlayer();
	}

	public static void setOwningPlayer(ItemType itemType, @Nullable OfflinePlayer player) {
		if (!(itemType.getItemMeta() instanceof SkullMeta skullMeta))
			return;

		if (player == null) {
			skullMeta.setOwningPlayer(null);
		} else if (REQUIRES_TEXTURE_LOOKUP) {
			PlayerProfile profile = player.getPlayerProfile();
			if (!profile.hasTextures())
				profile.complete(true); // BLOCKING MOJANG API CALL
			skullMeta.setPlayerProfile(profile);
		} else if (player.getName() != null) {
			skullMeta.setOwningPlayer(player);
		} else if (CAN_CREATE_PLAYER_PROFILE) {
			//noinspection deprecation
			skullMeta.setOwnerProfile(Bukkit.createPlayerProfile(player.getUniqueId(), ""));
		}
		itemType.setItemMeta(skullMeta);
	}

	public static void setOwningPlayer(ItemStack itemStack, @Nullable OfflinePlayer player) {
		if (!itemStack.hasItemMeta() || !(itemStack.getItemMeta() instanceof SkullMeta skullMeta))
			return;

		if (player == null) {
			skullMeta.setOwningPlayer(null);
		} else if (REQUIRES_TEXTURE_LOOKUP) {
			PlayerProfile profile = player.getPlayerProfile();
			if (!profile.hasTextures())
				profile.complete(true); // BLOCKING MOJANG API CALL
			skullMeta.setPlayerProfile(profile);
		} else if (player.getName() != null) {
			skullMeta.setOwningPlayer(player);
		} else if (CAN_CREATE_PLAYER_PROFILE) {
			//noinspection deprecation
			skullMeta.setOwnerProfile(Bukkit.createPlayerProfile(player.getUniqueId(), ""));
		}
		itemStack.setItemMeta(skullMeta);
	}

	public static void setOwningPlayer(Block block, @Nullable OfflinePlayer player) {
		if (!(block.getState() instanceof Skull skull))
			return;

		if (player == null) {
			//noinspection deprecation
			skull.setOwnerProfile(null);
		} else if (REQUIRES_TEXTURE_LOOKUP) {
			PlayerProfile profile = player.getPlayerProfile();
			if (!profile.hasTextures())
				profile.complete(true); // BLOCKING MOJANG API CALL
			skull.setPlayerProfile(profile);
		} else if (player.getName() != null) {
			skull.setOwningPlayer(player);
		} else if (CAN_CREATE_PLAYER_PROFILE) {
			//noinspection deprecation
			skull.setOwnerProfile(Bukkit.createPlayerProfile(player.getUniqueId(), ""));
		}
		skull.update();
	}

}
