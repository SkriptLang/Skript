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

import org.bukkit.boss.BarStyle;
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

@Name("BossBar Style")
@Description("The <a href='classes.html#bossbarstyle'>style</a> of a bossbar")
@Since("INSERT VERSION")
@Examples("set style of a new bossbar to solid")
public class ExprBossBarStyle extends SimpleExpression<BarStyle> {
	
	static {
		Skript.registerExpression(ExprBossBarStyle.class, BarStyle.class, ExpressionType.COMBINED, "style of [[boss[ ]]bar] %bossbar%", "[[boss[ ]]bar] %bossbar%'s style");
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
	protected BarStyle[] get(Event e) {
		BossBar bossBar = bar.getSingle(e);
		return bossBar == null ? null : new BarStyle[]{bossBar.getStyle()};
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case RESET:
				return new Class[]{BarStyle.class};
			default:
				return null;
		}
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		BarStyle style = delta != null ? (BarStyle) delta[0] : BarStyle.SOLID;
			bar.getSingle(e).setStyle(style);
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends BarStyle> getReturnType() {
		return BarStyle.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "style of bossbar " + bar.toString(e, debug);
	}
	
}
