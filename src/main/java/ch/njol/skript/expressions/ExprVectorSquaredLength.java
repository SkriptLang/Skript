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
package ch.njol.skript.expressions;

import org.bukkit.util.Vector;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Vectors - Squared Length")
@Description("Gets the squared length of a vector.")
@Examples("send \"%squared length of vector 1, 2, 3%\"")
@Since("2.2-dev28")
public class ExprVectorSquaredLength extends SimplePropertyExpression<Vector, Number> {

	static {
		register(ExprVectorSquaredLength.class, Number.class, "squared length[s]", "vectors");
	}

	@SuppressWarnings("unused")
	@Override
	public Number convert(Vector vector) {
		return vector.lengthSquared();
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return "squared length of vector";
	}


}
