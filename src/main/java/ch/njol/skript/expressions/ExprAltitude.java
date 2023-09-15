/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Location;

/**
 * @author Peter Güttinger
 */
@Name("Altitude")
@Description("Effectively an alias of 'y-<a href='#ExprCoordinate'>coordinate</a> of …', it represents the height of some object above bedrock.")
@Examples({"on damage:",
	"	altitude of the attacker is higher than the altitude of the victim",
	"	set damage to damage * 1.2"})
@Since("1.4.3")
public class ExprAltitude extends SimplePropertyExpression<Location, Number> {

	static {
		register(ExprAltitude.class, Number.class, "altitude[s]", "locations");
	}

	@Override
	public Number convert(final Location l) {
		return l.getY();
	}

	@Override
	protected String getPropertyName() {
		return "altitude";
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

}
