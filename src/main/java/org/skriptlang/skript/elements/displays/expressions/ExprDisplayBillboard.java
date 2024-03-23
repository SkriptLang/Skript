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
import org.bukkit.entity.Display.Billboard;
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

@Name("Display Billboard")
@Description({
	"Returns or changes the <a href='classes.html#billboard'>billboard</a> setting of <a href='classes.html#display'>displays</a>.",
	"This describes the axes/points around which the display can pivot.",
	"Displays spawn with the default setting as 'fixed'. Resetting this expression will also set it to 'fixed'."
})
@Examples("set billboard of the last spawned text display to center")
@RequiredPlugins("Spigot 1.19.4+")
@Since("INSERT VERSION")
public class ExprDisplayBillboard extends SimplePropertyExpression<Display, Billboard> {

	static {
		if (Skript.isRunningMinecraft(1, 19, 4))
			registerDefault(ExprDisplayBillboard.class, Billboard.class, "billboard[s]", "displays");
	}

	@Override
	@Nullable
	public Billboard convert(Display display) {
		return display.getBillboard();
	}

	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case ADD:
			case DELETE:
			case REMOVE:
			case REMOVE_ALL:
				break;
			case RESET:
				return CollectionUtils.array();
			case SET:
				return CollectionUtils.array(Billboard.class);
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (mode == ChangeMode.RESET) {
			for (Display display : getExpr().getArray(event))
				display.setBillboard(Billboard.FIXED);
			return;
		}
		Billboard billboard = (Billboard) delta[0];
		for (Display display : getExpr().getArray(event))
			display.setBillboard(billboard);
	}

	@Override
	public Class<? extends Billboard> getReturnType() {
		return Billboard.class;
	}

	@Override
	protected String getPropertyName() {
		return "billboard";
	}

}
