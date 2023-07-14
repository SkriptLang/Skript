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
import ch.njol.skript.util.Timespan;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

@Name("Exp Cooldown")
@Description("The exp cooldown of a player")
@Examples({
	"player's exp cooldown is less than 3 ticks:",
	    "\tsend \"increasing your exp cooldown!\" to player",
	    "\tset player's exp cooldown to 40 ticks"
})
@Since("2.7")
public class ExprExpCooldown  extends SimplePropertyExpression<Player, Timespan> {

	static {
		if (Skript.methodExists(Player.class, "getExpCooldown"))
			register(ExprExpCooldown.class, Timespan.class, "exp[erience] [pickup] cooldown change", "players");
	}

	@Override
	@Nullable
	public Timespan convert(Player player) {
		return Timespan.fromTicks_i(player.getExpCooldown());
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return (mode != ChangeMode.REMOVE_ALL) ? CollectionUtils.array(Timespan.class) : null;
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		int time = delta == null ? 0 : (int) ((Timespan) delta[0]).getTicks_i();
		int newTime;
		switch (mode) {
			case ADD:
				for (Player player : getExpr().getArray(e)) {
					newTime = player.getExpCooldown() + time;
					setExpCooldown(player, newTime);
				}
				break;
			case REMOVE:
				for (Player player : getExpr().getArray(e)) {
					newTime = player.getExpCooldown() - time;
					setExpCooldown(player, newTime);
				}
				break;
			case SET:
				for (Player player : getExpr().getArray(e)) {
					setExpCooldown(player, time);
				}
				break;
			case DELETE:
			case RESET:
				for (Player player : getExpr().getArray(e)) {
					setExpCooldown(player, 0);
				}
				break;
			default:
				assert false;
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() { return Timespan.class; }

	@Override
	protected String getPropertyName() {
		return "exp cooldown";
	}

	private void setExpCooldown(Player player, int ticks) {
		if (ticks < 0)
			ticks = 0;
		player.setExpCooldown(ticks);
	}

}
