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

@Name("Display Height/Width")
@Description({
	"Returns or changes the height or width of <a href='classes.html#display'>displays</a>.",
	"The rendering culling bounding box spans horizontally width/2 from entity position, "+
	"and the part beyond will be culled.",
	"If set to 0, no culling will occur on both the vertical and horizontal directions. Default is 0.0."
})
@Examples("set height of the last spawned text display to 2.5")
@RequiredPlugins("Spigot 1.19.4+")
@Since("INSERT VERSION")
public class ExprDisplayHeightWidth extends SimplePropertyExpression<Display, Float> {

	static {
		if (Skript.isRunningMinecraft(1, 19, 4))
			registerDefault(ExprDisplayHeightWidth.class, Float.class, "display (:height|width)", "displays");
	}

	private boolean height;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		height = parseResult.hasTag("height");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	@Nullable
	public Float convert(Display display) {
		return height ? display.getDisplayHeight() : display.getDisplayWidth();
	}

	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case ADD:
			case DELETE:
			case REMOVE:
			case RESET:
			case SET:
				return CollectionUtils.array(Number.class);
			case REMOVE_ALL:
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Display[] displays = getExpr().getArray(event);
		float change = delta == null ? 0F : ((Number) delta[0]).floatValue();
		change = Math.max(0F, change);
		switch (mode) {
			case REMOVE_ALL:
			case REMOVE:
				change = -change;
			case ADD:
				for (Display display : displays) {
					if (height) {
						float value = Math.max(0F, display.getDisplayHeight() + change);
						display.setDisplayHeight(value);
					} else {
						float value = Math.max(0F, display.getDisplayWidth() + change);
						display.setDisplayWidth(value);
					}
				}
				break;
			case DELETE:
			case RESET:
			case SET:
				for (Display display : displays) {
					if (height) {
						display.setDisplayHeight(change);
					} else {
						display.setDisplayWidth(change);
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
		return height ? "height" : "width";
	}

}
