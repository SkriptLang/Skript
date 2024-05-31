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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.EntityUnleashEvent.UnleashReason;
import org.eclipse.jdt.annotation.Nullable;

@Name("Unleash Reason")
@Description("The unleash reason in an unleash event.")
@Examples({
	"if the unleash reason is distance:",
		"\tbroadcast \"The leash was snapped in half.\""
})
@Events("Unleash")
@Since("INSERT VERSION")
public class ExprUnleashReason extends EventValueExpression<UnleashReason> {

	public ExprUnleashReason() {
		super(UnleashReason.class);
	}

	static {
		Skript.registerExpression(ExprUnleashReason.class, EntityUnleashEvent.UnleashReason.class, ExpressionType.SIMPLE, "[the] unleash reason");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(EntityUnleashEvent.class)) {
			Skript.error("The 'unleash reason' expression can only be used in an 'unleash' event");
			return false;
		}
		return true;
	}

	@Override
	protected UnleashReason[] get(Event event) {
		if (!(event instanceof EntityUnleashEvent))
			return new UnleashReason[0];
		return CollectionUtils.array(((EntityUnleashEvent) event).getReason());
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends UnleashReason> getReturnType() {
		return UnleashReason.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "unleash reason";
	}

}
