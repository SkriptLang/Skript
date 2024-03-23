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
package org.skriptlang.skript.elements.displays.text;

import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
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

@Name("Text Display Opacity")
@Description({
	"Returns or changes the opacity of <a href='classes.html#display'>text displays</a>.",
	"Values are between -127 and 127. The value of 127 represents it being completely opaque."
})
@Examples("set the opacity of the last spawned text display to -1 # Reset")
@RequiredPlugins("Spigot 1.19.4+")
@Since("INSERT VERSION")
public class ExprTextDisplayOpacity extends SimplePropertyExpression<Display, Byte> {

	static {
		if (Skript.isRunningMinecraft(1, 19, 4))
			registerDefault(ExprTextDisplayOpacity.class, Byte.class, "[display] opacity", "displays");
	}

	@Override
	@Nullable
	public Byte convert(Display display) {
		if (!(display instanceof TextDisplay))
			return null;
		return ((TextDisplay) display).getTextOpacity();
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
		byte change = delta == null ? -1 : ((Number) delta[0]).byteValue();
		change = (byte) Math.max(-127, change);
		switch (mode) {
			case REMOVE_ALL:
			case REMOVE:
				change = (byte) -change;
			case ADD:
				for (Display display : displays) {
					if (!(display instanceof TextDisplay))
						continue;
					TextDisplay textDisplay = (TextDisplay) display;
					byte value = (byte) Math.min(127, textDisplay.getTextOpacity() + change);
					value = (byte) Math.max(-127, value);
					textDisplay.setTextOpacity(value);
				}
				break;
			case DELETE:
			case RESET:
			case SET:
				for (Display display : displays) {
					if (!(display instanceof TextDisplay))
						continue;
					((TextDisplay) display).setTextOpacity(change);
				}
				break;
		}
	}

	@Override
	public Class<? extends Byte> getReturnType() {
		return Byte.class;
	}

	@Override
	protected String getPropertyName() {
		return "opacity";
	}

}
