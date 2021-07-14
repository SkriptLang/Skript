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
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.boss.BossBar;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("BossBar Progress")
@Description("The progress of a bossbar out of 100 (100 is a full bar, and 0 an empty one).")
@Since("INSERT VERSION")
@Examples("set progress of a new bossbar to 10")
public class ExprBossBarProgress extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprBossBarProgress.class, Number.class, ExpressionType.COMBINED, "progress of [[boss[ ]]bar] %bossbar%", "[[boss[ ]]bar] %bossbar%'s progress");
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
	protected Number[] get(Event e) {
		BossBar bossBar = bar.getSingle(e);
		return bossBar == null ? null : new Number[]{Math.round(bossBar.getProgress() * 100)};
	}

	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case REMOVE:
			case ADD:
			case RESET:
				return new Class[]{Number.class};
			default:
				return null;
		}
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null)
			return;
		double mod = 1;
		switch (mode) {
			case REMOVE:
				mod = -1;
			case ADD: {
				double change = ((Long) delta[0]) * mod / 100;
				BossBar bossBar = bar.getSingle(e);
				Double newProgress = bossBar.getProgress() + change;
				if (newProgress < 0 || newProgress > 1)
					return;
				bossBar.setProgress(newProgress);
				break;
			}
			case RESET:
				bar.getSingle(e).setProgress(0);
				break;
			case SET: {
				double change = ((Long) delta[0]) * mod / 100;
				if (change < 0 || change > 1)
					return;
				bar.getSingle(e).setProgress(change);
				break;
			}
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "progress of bossbar " + bar.toString(e, debug);
	}

}
