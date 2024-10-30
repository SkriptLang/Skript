package ch.njol.skript.expressions;

import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Skull Owner")
@Description("The skull owner of a player skull.")
@Examples({
	"set {_owner} to the skull owner of event-block",
	"set skull owner of {_block} to \"Njol\" parsed as offlineplayer"
})
@Since("2.9.0")
public class ExprSkullOwner extends SimplePropertyExpression<Block, OfflinePlayer> {

	static {
		register(ExprSkullOwner.class, OfflinePlayer.class, "(head|skull) owner", "blocks");
	}

	@Override
	public @Nullable OfflinePlayer convert(Block block) {
		BlockState state = block.getState();
		if (!(state instanceof Skull))
			return null;
		return ((Skull) state).getOwningPlayer();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(OfflinePlayer.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		OfflinePlayer offlinePlayer = (OfflinePlayer) delta[0];
		for (Block block : getExpr().getArray(event)) {
			BlockState state = block.getState();
			if (!(state instanceof Skull))
				continue;

			Skull skull = (Skull) state;
			if (offlinePlayer.getName() != null) {
				skull.setOwningPlayer(offlinePlayer);
			} else if (ItemUtils.CAN_CREATE_PLAYER_PROFILE) {
				//noinspection deprecation
				skull.setOwnerProfile(Bukkit.createPlayerProfile(offlinePlayer.getUniqueId(), ""));
			} else {
				//noinspection deprecation
				skull.setOwner("");
			}
			skull.update(true, false);
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
