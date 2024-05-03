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
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.util.coll.CollectionUtils;

@Name("Display Glow Color Override")
@Description({
	"Returns or changes the glowing color override of <a href='classes.html#display'>displays</a>.",
	"This overrides whatever color is already set for the scoreboard team of the displays."
})
@Examples("set glow color override of the last spawned text display to blue")
@RequiredPlugins("Spigot 1.19.4+")
@Since("INSERT VERSION")
public class ExprDisplayGlowOverride extends SimplePropertyExpression<Display, Color> {

	static {
		if (Skript.isRunningMinecraft(1, 19, 4))
			registerDefault(ExprDisplayGlowOverride.class, Color.class, "glow[ing] colo[u]r[s] [override[s]]", "displays");
	}

	@Override
	@Nullable
	public Color convert(Display display) {
		if (display.getGlowColorOverride() == null)
			return null;
		return ColorRGB.fromBukkitColor(display.getGlowColorOverride());
	}

	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(Color.class);
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Display[] displays = getExpr().getArray(event);
		if (mode != ChangeMode.SET) {
			for (Display display : displays)
				display.setGlowColorOverride(null);
			return;
		}
		if (delta == null)
			return;
		Color color = (Color) delta[0];
		for (Display display : displays)
			display.setGlowColorOverride(color.asBukkitColor());
	}

	@Override
	public Class<? extends Color> getReturnType() {
		return Color.class;
	}

	@Override
	protected String getPropertyName() {
		return "glow color override";
	}

}
