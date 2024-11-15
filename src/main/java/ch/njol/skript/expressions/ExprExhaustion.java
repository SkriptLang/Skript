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

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Exhaustion")
@Description("The exhaustion of a player. This is mainly used to determine the rate of hunger depletion.")
@Examples("set exhaustion of all players to 1")
@Since("2.2-dev35")
public class ExprExhaustion extends SimplePropertyExpression<Player, Number>{
	
	static {
		register(ExprExhaustion.class, Number.class, "exhaustion", "players");
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return "exhaustion";
	}

	@Override
	@Nullable
	public Number convert(Player player) {
		return player.getExhaustion();
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		return CollectionUtils.array(Number.class);
	}
	
	@SuppressWarnings("null")
	@Override
	public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
		float exhaustion = ((Number)delta[0]).floatValue();
		switch (mode) {
			case ADD:
				for (Player player : getExpr().getArray(event))
					player.setExhaustion(player.getExhaustion() + exhaustion);
				break;
			case REMOVE:
				for (Player player : getExpr().getArray(event))
					player.setExhaustion(player.getExhaustion() - exhaustion);
				break;
			case SET:
				for (Player player : getExpr().getArray(event))
					player.setExhaustion(((Number)delta[0]).floatValue());
				break;
			case DELETE:
			case REMOVE_ALL:
			case RESET:
				for (Player player : getExpr().getArray(event))
					player.setExhaustion(0);
				break;
		}
	}

}