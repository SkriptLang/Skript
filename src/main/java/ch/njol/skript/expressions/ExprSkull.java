package ch.njol.skript.expressions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Player Skull")
@Description("Gets a skull item representing a player. Skulls for other entities are provided by the aliases.")
@Examples({
	"give the victim's skull to the attacker",
	"set the block at the entity to the entity's skull"
})
@Since("2.0")
public class ExprSkull extends SimplePropertyExpression<OfflinePlayer, ItemType> {

	static {
		register(ExprSkull.class, ItemType.class, "(head|skull)", "offlineplayers");
	}

	@Override
	public @Nullable ItemType convert(OfflinePlayer player) {
		ItemType skull = new ItemType(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();

		if (player.getName() != null) {
			meta.setOwningPlayer(player);
		} else {
			//noinspection deprecation
			meta.setOwnerProfile(Bukkit.createPlayerProfile(player.getUniqueId(), ""));
		}

		skull.setItemMeta(meta);
		return skull;
	}

	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}

	@Override
	protected String getPropertyName() {
		return "skull";
	}

}
