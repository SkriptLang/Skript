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
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.WeatherType;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * @author Nicofisi
 * @author Peter Güttinger
 */
@Name("Player Weather")
@Description("The weather for a player.")
@Examples({"set weather of arg-player to rainy",
		"reset player's weather",
		"if arg-player's weather is rainy"})
@Since("2.2-dev34")
public class ExprPlayerWeather extends SimplePropertyExpression<Player, WeatherType> {

	static {
		register(ExprPlayerWeather.class, WeatherType.class, "[(client|custom)] weather", "players");
	}

	@Override
	protected String getPropertyName() {
		return "weather";
	}

	@Override
	@Nullable
	public WeatherType convert(Player player) {
		return WeatherType.fromPlayer(player);
	}
	
	@Override
	public Class<WeatherType> getReturnType() {
		return WeatherType.class;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.RESET || mode == ChangeMode.SET)
			return CollectionUtils.array(WeatherType.class);
		return null;
	}

	@SuppressWarnings("null")
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		if (delta == null) {
			for (Player p : getExpr().getArray(e))
				p.resetPlayerWeather();
		} else {
			WeatherType type = (WeatherType) delta[0];
			for (Player p : getExpr().getArray(e))
				type.setWeather(p);
		}
	}

}