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
package org.skriptlang.skript.elements.displays.expressions;

import org.bukkit.entity.Display;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Display Shadow Radius/Strength")
@Description("Returns or changes the shadow radius/strength of <a href='classes.html#display'>displays</a>.")
@Examples("set shadow radius of the last spawned text display to 1.75")
@RequiredPlugins("Spigot 1.19.4+")
@Since("INSERT VERSION")
public class ExprDisplayShadow extends SimplePropertyExpression<Display, Float> {

	static {
		if (Skript.isRunningMinecraft(1, 19, 4))
			registerDefault(ExprDisplayShadow.class, Float.class, "shadow (:radius|strength)", "displays");
	}

	private boolean radius;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		radius = parseResult.hasTag("radius");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	@Nullable
	public Float convert(Display display) {
		return radius ? display.getShadowRadius() : display.getShadowStrength();
	}

	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return CollectionUtils.array(Number.class);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Display[] displays = getExpr().getArray(event);
		float change = delta == null ? 0F : (int) ((Number) delta[0]).floatValue();
		change = Math.max(0F, change);
		switch (mode) {
			case REMOVE_ALL:
			case REMOVE:
				change = -change;
			case ADD:
				for (Display display : displays) {
					if (radius) {
						float value = Math.max(0F, display.getShadowRadius() + change);
						display.setShadowRadius(value);
					} else {
						float value = Math.max(0F, display.getShadowStrength() + change);
						display.setShadowStrength(value);
					}
				}
				break;
			case DELETE:
			case RESET:
				for (Display display : displays) {
					if (radius) {
						display.setShadowRadius(0F);
					} else {
						display.setShadowStrength(0F);
					}
				}
				break;
			case SET:
				for (Display display : displays) {
					if (radius) {
						display.setShadowRadius(change);
					} else {
						display.setShadowStrength(change);
					}
				}
				break;
		}
	}

	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return radius ? "radius" : "strength";
	}

}
