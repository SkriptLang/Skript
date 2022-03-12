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
		register(ExprTimespanDetails.class, Long.class, "(0:second[s]|1:minute[s]|2:hour[s]|3:day[s]|4:week[s]|5:month[s]|6:year[s])", "timespans");
	}

	private final int SECONDS = 0, MINUTES = 1, HOURS = 2, DAYS = 3, WEEKS = 4, MONTHS = 5, YEARS = 6;
	private int type;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<? extends Timespan>) exprs[0]);
		type = Integer.parseInt(parseResult.tags.get(0));
		return true;
	}

	@Override
	@Nullable
	public Long convert(Timespan t) {
		long time;

		if (type == YEARS)
			time = t.getYears();
		else if (type == MONTHS)
			time = t.getMonths();
		else if (type == WEEKS)
			time = t.getWeeks();
		else if (type == DAYS)
			time = t.getDays();
		else if (type == HOURS)
			time = t.getHours();
		else if (type == MINUTES)
			time = t.getMinutes();
		else
			time = t.getSeconds();

		return time;
	}

	@Override
	protected String getPropertyName() {
		String r;

		if (type == YEARS)
			r = "years";
		else if (type == MONTHS)
			r = "months";
		else if (type == WEEKS)
			r = "weeks";
		else if (type == DAYS)
			r = "days";
		else if (type == HOURS)
			r = "hours";
		else if (type == MINUTES)
			r = "minutes";
		else
			r = "seconds";

		return r;
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

}
