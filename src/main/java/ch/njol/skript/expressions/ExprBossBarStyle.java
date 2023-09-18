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
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Boss Bar Style")
@Description("The style of a boss bar. This changes how the boss bar displays on the player's screen.")
@Examples({"set style of player's bossbar to 10 segments"})
@Since("INSERT VERSION")
public class ExprBossBarStyle extends SimplePropertyExpression<BossBar, BarStyle> {

	static {
		register(ExprBossBarStyle.class, BarStyle.class, "style", "bossbars");
	}

	@Override
	@Nullable
	public BarStyle convert(BossBar bossBar) {
		return bossBar.getStyle();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return new Class[] {BarStyle.class};
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		assert delta[0] != null;
		BarStyle style = (BarStyle) delta[0];
		for (BossBar bossBar : getExpr().getArray(event))
			bossBar.setStyle(style);
	}

	@Override
	public Class<BarStyle> getReturnType() {
		return BarStyle.class;
	}

	@Override
	protected String getPropertyName() {
		return "style";
	}

}
