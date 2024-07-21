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
package org.skriptlang.skript.bukkit.displays.expressions;

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
import ch.njol.util.coll.CollectionUtils;

@Name("Display View Range")
@Description({
	"Returns or changes the view range of <a href='classes.html#display'>displays</a>.",
	"Default value is 1.0. This value is then multiplied by 64 and the player's entity view distance setting to determine the actual range.",
	"For example, a player with 150% entity view distance will see a block display with a view range of 1.2 at 1.2 * 64 * 150% = 115.2 blocks away."
})
@Examples("set view range of the last spawned text display to 2.9")
@RequiredPlugins("Spigot 1.19.4+")
@Since("INSERT VERSION")
public class ExprDisplayViewRange extends SimplePropertyExpression<Display, Float> {

	static {
		if (Skript.isRunningMinecraft(1, 19, 4))
			registerDefault(ExprDisplayViewRange.class, Float.class, "[display] view (range|radius)", "displays");
	}

	@Override
	public @Nullable Float convert(Display display) {
		return display.getViewRange();
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, SET, REMOVE -> CollectionUtils.array(Number.class);
			case RESET -> CollectionUtils.array();
			case DELETE, REMOVE_ALL -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Display[] displays = getExpr().getArray(event);
		float change = delta == null ? 1F : ((Number) delta[0]).floatValue();
		if (Float.isNaN(change) || Float.isInfinite(change))
			return;
		switch (mode) {
			case REMOVE:
				change = -change;
			case ADD:
				for (Display display : displays) {
					float value = Math.max(0F, display.getViewRange() + change);
					display.setViewRange(value);
				}
				break;
			case RESET:
			case SET:
				change = Math.max(0F, change);
				for (Display display : displays)
					display.setViewRange(change);
				break;
		}
	}

	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return  "view range";
	}

}
