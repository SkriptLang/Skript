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

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityTameEvent;
import org.jetbrains.annotations.Nullable;

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

@Name("Tamer")
@Description("The tamer of an entity. Can only be used in entity tame events. You can use 'event-entity' to refer tamed entity itself.")
@Examples({"on tame:",
			"\tif the tamer is a player:",
			"\t\tsend \"someone tamed something!\" to console"})
@Since("2.2-dev25")
public class ExprTamer extends SimpleExpression<Player> {
	
	static {
		Skript.registerExpression(ExprTamer.class, Player.class, ExpressionType.SIMPLE, "[the] tamer");
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		if (!getParser().isCurrentEvent(EntityTameEvent.class)) {
			Skript.error("the expression 'tamer' may only be used in the entity tame event.");
			return false;
		}
		return true;
	}
	
	@Override
	protected Player[] get(final Event e) {
		if (!(e instanceof EntityTameEvent))
			return null;

		return new Player[] {((EntityTameEvent) e).getOwner() instanceof Player ? (Player) ((EntityTameEvent) e).getOwner() : null};
	}
	
	@Override
	public Class<? extends Player> getReturnType() {
		return Player.class;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the tamer";
	}
}
