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
import ch.njol.util.Kleenean;
import org.eclipse.jdt.annotation.Nullable;

@Name("Timespan Details")
@Description("Retrieve specific information of a <a href=\"/classes.html#timespan\">timespan</a> such as hours/minutes/etc.")
@Examples({
	"set {_t} to difference between now and {Payouts::players::%uuid of player%::last-date}",
	"send \"It has been %days of {_t}% day(s) since last payout.\""
})
@Since("INSERT VERSION")
public class ExprTimespanDetails extends SimplePropertyExpression<Timespan, Long> {

	static {
		register(ExprTimespanDetails.class, Long.class, "(0:tick[s]|1:second[s]|2:minute[s]|3:hour[s]|4:day[s]|5:week[s]|6:month[s]|7:year[s])", "timespans");
	}

	private final int TICKS = 0, SECONDS = 1, MINUTES = 2, HOURS = 3, DAYS = 4, WEEKS = 5, MONTHS = 6, YEARS = 7;
	private int type;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<? extends Timespan>) exprs[0]);
		type = parseResult.mark;
		return true;
	}

	@Override
	@Nullable
	public Long convert(Timespan t) {
		switch (type) {
			case YEARS:
				return t.getYears();
			case MONTHS:
				return t.getMonths();
			case WEEKS:
				return t.getWeeks();
			case DAYS:
				return t.getDays();
			case HOURS:
				return t.getHours();
			case MINUTES:
				return t.getMinutes();
			case SECONDS:
				return t.getSeconds();
		}

		return t.getTicks_i();
	}

	@Override
	protected String getPropertyName() {
		switch (type) {
			case YEARS:
				return "years";
			case MONTHS:
				return "months";
			case WEEKS:
				return "weeks";
			case DAYS:
				return "days";
			case HOURS:
				return "hours";
			case MINUTES:
				return "minutes";
			case SECONDS:
				return "seconds";
		}

		return "ticks";
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

}
