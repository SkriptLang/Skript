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
package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;

@Name("Is Invisible")
@Description("Checks whether a living entity or a boss bar is invisible.")
@Examples("target entity is invisible")
@Since("2.7")
public class CondIsInvisible extends PropertyCondition<Object> {

	static {
		if (Skript.methodExists(LivingEntity.class, "isInvisible"))
			register(CondIsInvisible.class, PropertyType.BE, "(invisible|:visible)", "livingentities/bossbars");
	}

	private boolean visible;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		setNegated(matchedPattern == 1 ^ (visible = parseResult.hasTag("visible")));
		return true;
	}

	@Override
	public boolean check(Object target) {
		if (target instanceof LivingEntity entity) {
			return entity.isInvisible();
		} else if (target instanceof BossBar bar) {
			return !bar.isVisible();
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return visible ? "visible" : "invisible";
	}

}
