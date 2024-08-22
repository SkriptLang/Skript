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
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.registrations.EventValues;

import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

@Name("Heal Reason")
@Description("The <a href='./classes.html#healreason'>heal reason</a> of a <a href='./events.html#heal'>heal event</a>.")
@Examples({
	"on heal:",
		"\theal reason is satiated",
		"\tsend \"You ate enough food and gained full health back!\""
})
@Events("heal")
@Since("2.5")
public class ExprHealReason extends EventValueExpression<RegainReason> {

	static {
		register(ExprHealReason.class, RegainReason.class, "(regen|health regain|heal[ing]) (reason|cause)");
	}

	public ExprHealReason() {
		super(RegainReason.class);
	}

	@Override
	public boolean setTime(int time) {
		if (time == EventValues.TIME_FUTURE)
			return false;
		return super.setTime(time);
	}

}
