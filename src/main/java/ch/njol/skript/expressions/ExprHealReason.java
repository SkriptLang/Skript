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

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;


@Name("Heal Reason")
@Description("The <a href='../classes.html#healreason'>heal reason</a> of a heal event. Please click on the link for more information.")
@Examples({"on heal:",
	"\tif heal reason = satiated:",
	"\t\tsend \"You ate enough food and gained health back!\" to player"})
@Since("2.5")
public class ExprHealReason extends SimpleExpression<RegainReason> {
	
	static {
		Skript.registerExpression(ExprHealReason.class, RegainReason.class, ExpressionType.SIMPLE, "(regen|health regain|heal) (reason|cause)");
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(EntityRegainHealthEvent.class)) {
			Skript.error("Heal reason can only be used in an on heal event", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}
	
	@Nullable
	@Override
	protected RegainReason[] get(Event e) {
		return new RegainReason[]{((EntityRegainHealthEvent) e).getRegainReason()};
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends RegainReason> getReturnType() {
		return RegainReason.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "heal reason";
	}
	
}
