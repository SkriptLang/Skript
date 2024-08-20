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
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.boss.BossBar;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("Boss Bar Progress")
@Description("The progress of a boss bar (ranges from 0 to 100)")
@Examples({"set the progress of {_bar} to 56"})
@Since("INSERT VERSION")
public class ExprBossBarProgress extends SimplePropertyExpression<BossBar, Double> {

	static {
		register(ExprBossBarProgress.class, Double.class, "progress", "bossbars");
	}

	@Override
	public @NotNull Double convert(BossBar bossBar) {
		return bossBar.getProgress() * 100;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE -> new Class[] {Number.class};
			case RESET -> new Class[0];
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object[] delta, ChangeMode mode) {
		for (BossBar bar : getExpr().getArray(event)) {
			double current = this.convert(bar).doubleValue();
			double change = delta != null && delta.length > 0 ? ((Number) delta[0]).doubleValue() : 0;
			bar.setProgress(Math.max(0, Math.min(1, switch (mode) {
				case ADD -> current + change;
				case REMOVE -> current - change;
				case SET -> change;
				default -> 0;
			} / 100)));
		}
	}

	@Override
	public Class<Double> getReturnType() {
		return Double.class;
	}

	@Override
	protected String getPropertyName() {
		return "progress";
	}

}
