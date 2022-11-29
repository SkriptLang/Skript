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

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("World Border")
@Description({
	"Get the border of a world or a player.",
	"NOTE: Player world borders are only available in Minecraft 1.18+."
})
@Examples("set {_border} to world border of player's world")
@Since("INSERT VERSION")
public class ExprWorldBorder extends SimplePropertyExpression<Object, WorldBorder> {

	static {
		if (Skript.methodExists(Player.class, "getWorldBorder")) {
			register(ExprWorldBorder.class, WorldBorder.class, "[world[ ]]border", "worlds/players");
		} else {
			register(ExprWorldBorder.class, WorldBorder.class, "[world[ ]]border", "worlds");
		}
	}

	@Override
	@Nullable
	public WorldBorder convert(Object object) {
		if (object instanceof World)
			return ((World) object).getWorldBorder();
		Player player = (Player) object;
		return player.getWorldBorder() == null ? player.getWorld().getWorldBorder() : player.getWorldBorder();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return mode == ChangeMode.SET || mode == ChangeMode.RESET ? CollectionUtils.array(WorldBorder.class) : null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Object[] objects = getExpr().getArray(event);
		if (mode == ChangeMode.RESET) {
			for (Object object : objects) {
				if (object instanceof World) {
					((World) object).getWorldBorder().reset();
				} else {
					((Player) object).setWorldBorder(null);
				}
			}
			return;
		}
		WorldBorder to = (WorldBorder) delta[0];
		assert to != null;
		for (Object object : objects) {
			if (object instanceof World) {
				WorldBorder worldBorder = ((World) object).getWorldBorder();
				worldBorder.setCenter(to.getCenter());
				worldBorder.setSize(to.getSize());
				worldBorder.setDamageAmount(to.getDamageAmount());
				worldBorder.setDamageBuffer(to.getDamageBuffer());
				worldBorder.setWarningDistance(to.getWarningDistance());
				worldBorder.setWarningTime(to.getWarningTime());
			} else {
				((Player) object).setWorldBorder(to);
			}
		}
	}

	@Override
	protected String getPropertyName() {
		return "world border";
	}

	@Override
	public Class<? extends WorldBorder> getReturnType() {
		return WorldBorder.class;
	}

}
