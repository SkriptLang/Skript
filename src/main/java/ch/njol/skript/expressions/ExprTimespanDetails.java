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

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@Name("Timespan Details")
@Description("Retrieve specific information of a <a href=\"/classes.html#timespan\">timespan</a> such as hours/minutes/etc.")
@Examples({
	"set {_t} to difference between now and {Payouts::players::%uuid of player%::last-date}",
	"send \"It has been %days of {_t}% day(s) since last payout.\""
})
@Since("2.9.0")
public class ExprTimespanDetails extends SimplePropertyExpression<Timespan, Long> {

	static {
		register(ExprTimespanDetails.class, Long.class, "(:(tick|second|minute|hour|day|week|month|year))s", "timespans");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private TimePeriod type;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		type = TimePeriod.valueOf(parseResult.tags.get(0).toUpperCase(Locale.ENGLISH));
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	@Nullable
	public Long convert(Timespan time) {
		return time.getAs(Timespan.TimePeriod.MILLISECOND) / type.getTime();
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	protected String getPropertyName() {
		return type.name().toLowerCase(Locale.ENGLISH);
	}

}
