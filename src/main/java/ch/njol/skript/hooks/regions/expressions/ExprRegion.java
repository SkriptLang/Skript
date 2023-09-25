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
package ch.njol.skript.hooks.regions.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.hooks.regions.classes.Region;

@Name("Region")
@Description({
	"The <a href='./classes.html#region'>region</a> involved in an event.",
	"This expression requires a supported regions plugin to be installed."
})
@Examples({
	"on region enter:",
		"\tregion is {forbidden region}",
		"\tcancel the event"
})
@Since("2.1")
@RequiredPlugins("Supported regions plugin")
public class ExprRegion extends EventValueExpression<Region> {

	static {
		register(ExprRegion.class, Region.class, "[event-]region");
	}

	public ExprRegion() {
		super(Region.class);
	}

}
