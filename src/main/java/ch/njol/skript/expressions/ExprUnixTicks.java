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

import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Date;

@Name("Unix Timestamp")
@Description("Converts given date to Unix timestamp. This is roughly how many seconds have elapsed since 1 January 1970.")
@Examples("unix timestamp of now")
@Since("2.2-dev31")
public class ExprUnixTicks extends SimplePropertyExpression<Date, Number> {
	
	static {
		register(ExprUnixTicks.class, Number.class, "unix timestamp", "dates");
	}

	@Override
	@Nullable
	public Number convert(Date f) {
		return f.getTimestamp() / 1000.0;
	}

	@Override
	protected String getPropertyName() {
		return "unix timestamp";
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
}
