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
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Hanging Entity/Remover")
@Description("Returns the hanging entity or remover.")
@Examples({"on break of item frame:",
		"\tif item of hanging entity is diamond pickaxe:",
		"\t\tcancel event",
		"\t\tif hanging remover is a player:",
		"\t\t\tsend \"You can't break that item frame!\" to hanging remover"})
@Since("INSERT VERSION")
public class ExprHanging extends SimpleExpression<Entity> {
	
	static {
		Skript.registerExpression(ExprHanging.class, Entity.class, ExpressionType.SIMPLE, "hanging (:entity|:remover)");
	}

	private boolean isRemover;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isRemover = parseResult.hasTag("remover");

		if (isRemover && !getParser().isCurrentEvent(HangingBreakEvent.class)) {
			Skript.error("The expression 'hanging remover' can only be used in break event");
			return false;
		} else if (!getParser().isCurrentEvent(HangingBreakEvent.class, HangingPlaceEvent.class)) {
			Skript.error("The expression 'hanging entity' can only be used in break and place events");
			return false;
		}
		return true;
	}
	
	@Override
	@Nullable
	protected Entity[] get(Event e) {
		if (!(e instanceof HangingEvent))
			return null;

		Entity entity;
		if (!(e instanceof HangingBreakByEntityEvent))
			entity = isRemover ? null : ((HangingEvent) e).getEntity();
		else
			entity = isRemover ? ((HangingBreakByEntityEvent) e).getRemover() : ((HangingEvent) e).getEntity();

		return new Entity[] { entity };
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}
	
	@Override
	@SuppressWarnings("null")
	public String toString(@Nullable Event e, boolean debug) {
		return "hanging " + (isRemover ? "remover" : "entity");
	}
	
}
