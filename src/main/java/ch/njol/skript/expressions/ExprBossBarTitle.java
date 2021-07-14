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

@Name("BossBar Title")
@Description("The title of a bossbar")
@Since("INSERT VERSION")
@Examples("set title of a new bossbar to \"example\"")
public class ExprBossBarTitle extends SimpleExpression<String> {
	
	static {
		Skript.registerExpression(ExprBossBarTitle.class, String.class, ExpressionType.COMBINED, "title of [[boss[ ]]bar] %bossbar%", "[[boss[ ]]bar] %bossbar%'s title");
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
	protected String[] get(Event e) {
		BossBar bossBar = bar.getSingle(e);
		return bossBar == null ? null : new String[]{bossBar.getTitle()};
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
				return new Class[]{String.class};
			default:
				return null;
		}
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta != null) {
			bar.getSingle(e).setTitle((String) delta[0]);
		}
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "title of bossbar " + bar.toString(e, debug);
	}
	
}
