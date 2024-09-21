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
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Game Mode")
@Description("The gamemode of a player. (<a href=\"classes.html#gamemode\">Gamemodes</a>)")
@Examples({"player's gamemode is survival",
		"set the player's gamemode to creative"})
@Since("1.0")
public class ExprGameMode extends PropertyExpression<Player, GameMode> {
	
	static {
		Skript.registerExpression(ExprGameMode.class, GameMode.class, ExpressionType.PROPERTY, "[the] game[ ]mode of %players%", "%players%'[s] game[ ]mode");
	}
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		setExpr((Expression<Player>) vars[0]);
		return true;
	}
	
	@Override
	protected GameMode[] get(final Event e, final Player[] source) {
		return get(source, player -> {
			if (getTime() >= 0 && e instanceof PlayerGameModeChangeEvent && ((PlayerGameModeChangeEvent) e).getPlayer() == player && !Delay.isDelayed(e))
				return ((PlayerGameModeChangeEvent) e).getNewGameMode();
			return player.getGameMode();
		});
	}
	
	@Override
	public Class<GameMode> getReturnType() {
		return GameMode.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the gamemode of " + getExpr().toString(e, debug);
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET)
			return CollectionUtils.array(GameMode.class);
		return null;
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
		final GameMode m = delta == null ? Bukkit.getDefaultGameMode() : (GameMode) delta[0];
		for (final Player p : getExpr().getArray(e)) {
			if (getTime() >= 0 && e instanceof PlayerGameModeChangeEvent && ((PlayerGameModeChangeEvent) e).getPlayer() == p && !Delay.isDelayed(e)) {
				if (((PlayerGameModeChangeEvent) e).getNewGameMode() != m)
					((PlayerGameModeChangeEvent) e).setCancelled(true);
			}
			p.setGameMode(m);
		}
	}
	
	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, PlayerGameModeChangeEvent.class);
	}
	
}
