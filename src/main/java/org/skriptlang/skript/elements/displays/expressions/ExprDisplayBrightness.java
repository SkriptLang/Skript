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
import org.bukkit.entity.Display.Brightness;
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

@Name("Display Brightness")
@Description({
	"Returns or changes the block or sky brightness of <a href='classes.html#display'>displays</a>.",
	"Value must be between 0 and 15."
})
@Examples("set sky brightness of the last spawned text display to 5")
@RequiredPlugins("Spigot 1.19.4+")
@Since("INSERT VERSION")
public class ExprDisplayBrightness extends SimplePropertyExpression<Display, Integer> {

	static {
		if (Skript.isRunningMinecraft(1, 19, 4))
			register(ExprDisplayBrightness.class, Integer.class, "(:sky|block) [light] brightness[es]", "displays");
	}

	private boolean sky;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		sky = parseResult.hasTag("sky");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	@Nullable
	public Integer convert(Display display) {
		Brightness brightness = display.getBrightness();
		return sky ? brightness.getSkyLight() : brightness.getBlockLight();
	}

	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return CollectionUtils.array(Number.class);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Display[] displays = getExpr().getArray(event);
		int change = delta == null ? 0 : (int) ((Number) delta[0]).intValue();
		switch (mode) {
			case REMOVE_ALL:
			case REMOVE:
				change = -change;
			case ADD:
				Brightness brightness;
				for (Display display : displays) {
					Brightness current = display.getBrightness();
					if (sky) {
						int value = current.getSkyLight() + change;
						value = Math.max(0, Math.min(15, value));
						brightness = new Brightness(current.getBlockLight(), value);
					} else {
						int value = current.getBlockLight() + change;
						value = Math.max(0, Math.min(15, value));
						brightness = new Brightness(value, current.getSkyLight());
					}
					display.setBrightness(brightness);
				}
				break;
			case DELETE:
			case RESET:
				for (Display display : displays)
					display.setBrightness(null);
				break;
			case SET:
				change = Math.max(0, Math.min(15, change));
				for (Display display : displays) {
					Brightness current = display.getBrightness();
					if (sky) {
						brightness = new Brightness(current.getBlockLight(), change);
					} else {
						brightness = new Brightness(change, current.getSkyLight());
					}
					display.setBrightness(brightness);
				}
				break;
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return (sky ? "sky" : "block") + "brightness";
	}

}
