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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.boss.BarFlag;
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

@Name("BossBar Flags")
@Description("The <a href='classes.html#bossbarflag'>flags</a> of a bossbar")
@Examples("add darken sky to flags of a new bossbar")
@Since("INSERT VERSION")
public class ExprBossBarFlags extends SimpleExpression<BarFlag> {
	
	static {
		Skript.registerExpression(ExprBossBarFlags.class, BarFlag.class, ExpressionType.COMBINED, "flags of [[boss[ ]]bar] %bossbar%", "[[boss[ ]]bar] %bossbar%'s flags");
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
	protected BarFlag[] get(Event e) {
		BossBar bossBar = bar.getSingle(e);
		if (bossBar == null)
			return null;
		ArrayList<BarFlag> flags = new ArrayList<>();
		for (BarFlag barFlag : BarFlag.values()) {
			if (bossBar.hasFlag(barFlag))
				flags.add(barFlag);
		}
		return flags.toArray(new BarFlag[0]);
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case REMOVE:
			case ADD:
				return new Class[]{BarFlag.class};
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
						bossBar.addFlag((BarFlag) o);
					}
					//This is done as flags for pre existing players aren't updated until they're readded to the bossbar
					List<Player> players = bossBar.getPlayers();
					bossBar.removeAll();
					for (Player player : players) {
						bossBar.addPlayer(player);
					}
				}
				break;
			case REMOVE:
				if (delta != null) {
					BossBar bossBar = bar.getSingle(e);
					for (Object o : delta) {
						bossBar.removeFlag((BarFlag) o);
					}
					//This is done as flags for pre existing players aren't updated until they're readded to the bossbar
					List<Player> players = bossBar.getPlayers();
					bossBar.removeAll();
					for (Player player : players) {
						bossBar.addPlayer(player);
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
	public Class<? extends BarFlag> getReturnType() {
		return BarFlag.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "flags of bossbar " + bar.toString(e, debug);
	}
	
}
