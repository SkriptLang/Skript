/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Skull Owner")
@Description("The skull owner of a player skull.")
@Examples({
	"set {_owner} to the skull owner of event-block",
	"set skull owner of {_block} to player"
})
@Since("INSERT VERSION")
public class ExprSkullOwner extends SimplePropertyExpression<Block, OfflinePlayer> {

	static {
		register(ExprSkullOwner.class, OfflinePlayer.class, "(head|skull) owner", "blocks");
	}

	@Override
	public @Nullable OfflinePlayer convert(Block block) {
		if (block.getType().equals(Material.PLAYER_HEAD) || block.getType().equals(Material.PLAYER_WALL_HEAD)) {
			return ((Skull) block.getState()).getOwningPlayer();
		}
		return null;
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(OfflinePlayer.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		for (Block block : getExpr().getArray(event)) {
			if (block.getType().equals(Material.PLAYER_HEAD) || block.getType().equals(Material.PLAYER_WALL_HEAD)) {
				Skull skull = (Skull) block.getState();
				skull.setOwningPlayer((OfflinePlayer) delta[0]);
				skull.update();
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
