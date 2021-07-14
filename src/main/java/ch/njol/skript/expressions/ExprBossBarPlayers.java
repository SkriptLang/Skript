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

import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("BossBar Players")
@Description("The players in a bossbar.")
@Examples({"set {_b} to bossbar with id \"example\"",
	"send \"%size of {_b}'s players%\""})
@Since("INSERT VERSION")
public class ExprBossBarPlayers extends SimpleExpression<Player> {
	
	static {
		Skript.registerExpression(ExprBossBarPlayers.class, Player.class, ExpressionType.COMBINED, "players of [[boss[ ]]bar] %bossbar%", "[[boss[ ]]bar] %bossbar%'s players");
	}
	
	@SuppressWarnings("null")
	Expression<BossBar> bar;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		bar = (Expression<BossBar>) exprs[0];
		return true;
	}
	
	@Nullable
	@Override
	protected Player[] get(Event e) {
		BossBar bossBar = bar.getSingle(e);
		return bossBar == null ? null : bossBar.getPlayers().toArray(new Player[0]);
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case REMOVE:
			case ADD:
				return new Class[]{Player.class};
			default:
				return null;
		}
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		switch (mode) {
			case ADD:
				if (delta != null) {
					BossBar bossBar = bar.getSingle(e);
					for (Object o : delta) {
						bossBar.addPlayer((Player) o);
					}
				}
				break;
			case REMOVE:
				if (delta != null) {
					BossBar bossBar = bar.getSingle(e);
					for (Object o : delta) {
						bossBar.removePlayer((Player) o);
					}
				}
				break;
		}
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends Player> getReturnType() {
		return Player.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "title of bossbar " + bar.toString(e, debug);
	}
	
}
