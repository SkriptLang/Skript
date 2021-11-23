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
package ch.njol.skript.effects;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

public class EffToggle extends Effect {
 
	static {
		// Skript.registerEffect(EffToggle.class, "toggle %boolean%");
	}
 
	private Expression<Boolean> expression;
 
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		expression = (Expression<Boolean>) exprs[0];
		if (!CollectionUtils.containsSuperclass(expression.acceptChange(ChangeMode.SET), Boolean.class)) {
			Skript.error(expression.toString(null, false) + " cannot be toggled");
      return false;
		}
    return true;
	}
 
	@Override
	protected void execute(Event e) {
		Boolean b = expression.getSingle(e);
		if (b != null)
			expression.change(e, new Boolean[] {!b}, ChangeMode.SET);
	}
 
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "toggle " + expression.toString(e, debug);
	}
 
}
