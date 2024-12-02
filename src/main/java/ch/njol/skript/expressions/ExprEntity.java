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

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
@Name("Creature/Entity/Player/Projectile/Villager/Powered Creeper/etc.")
@Description({"The entity involved in an event (an entity is a player, a creature or an inanimate object like ignited TNT, a dropped item or an arrow).",
		"You can use the specific type of the entity that's involved in the event, e.g. in a 'death of a creeper' event you can use 'the creeper' instead of 'the entity'."})
@Examples({"give a diamond sword of sharpness 3 to the player",
		"kill the creeper",
		"kill all powered creepers in the wolf's world",
		"projectile is an arrow"})
@Since("1.0")
public class ExprEntity extends SimpleExpression<Entity> {
	static {
		Skript.registerExpression(ExprEntity.class, Entity.class, ExpressionType.PATTERN_MATCHES_EVERYTHING, "[the] [event-]<.+>");
	}
	
	@SuppressWarnings("null")
	private EntityData<?> type;
	
	@SuppressWarnings("null")
	private EventValueExpression<Entity> entity;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		final RetainingLogHandler log = SkriptLogger.startRetainingLog();
		try {
			if (!StringUtils.startsWithIgnoreCase(parseResult.expr, "the ") && !StringUtils.startsWithIgnoreCase(parseResult.expr, "event-")) {
				
				String s = parseResult.regexes.get(0).group();
				final ItemType item = Aliases.parseItemType("" + s);
				log.clear();
				if (item != null) {
					log.printLog();
					return false;
				}
				
				//if(!StringUtils.startsWithIgnoreCase(parseResult.expr, "the event-") && !s.equalsIgnoreCase("player") && !s.equalsIgnoreCase("entity")){
				//	return false;
				//}
				
			}
			
			final EntityData<?> type = EntityData.parseWithoutIndefiniteArticle("" + parseResult.regexes.get(0).group());
			log.clear();
			log.printLog();
			if (type == null || type.isPlural().isTrue())
				return false;
			this.type = type;
		} finally {
			log.stop();
		}
		entity = new EventValueExpression<>(type.getType());
		return entity.init();
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
		return type.getType();
	}
	
	@Override
	@Nullable
	protected Entity[] get(final Event e) {
		final Entity[] es = entity.getArray(e);
		if (es.length == 0 || type.isInstance(es[0]))
			return es;
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		for (Class<R> t : to) {
			if (t.equals(EntityData.class)) {
				return new SimpleLiteral<>((R) type, false);
			}
		}
		return super.getConvertedExpression(to);
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the " + type;
	}
	
}
