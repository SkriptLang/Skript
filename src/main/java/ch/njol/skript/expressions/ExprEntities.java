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
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.log.LogHandler;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.iterator.CheckedIterator;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Name("Entities")
@Description("All entities in all worlds, in a specific world, in a chunk or in a radius around a certain location, " +
		"e.g. <code>all players</code>, <code>all creepers in the player's world</code>, or <code>players in radius 100 of the player</code>.")
@Examples({"kill all creepers in the player's world",
		"send \"Psst!\" to all players within 100 meters of the player",
		"give a diamond to all ops",
		"heal all tamed wolves in radius 2000 around {town center}",
		"delete all monsters in chunk at player"})
@Since("1.2.1, 2.5 (chunks)")
public class ExprEntities extends SimpleExpression<Entity> {

	static {
		Skript.registerExpression(ExprEntities.class, Entity.class, ExpressionType.PATTERN_MATCHES_EVERYTHING,
				"[(all [[of] the]|the)] %*entitydatas% [(in|of) ([world[s]] %-worlds%|1¦%-chunks%)]",
				"[(all [[of] the]|the)] entities of type[s] %entitydatas% [(in|of) ([world[s]] %-worlds%|1¦%-chunks%)]",
				"[(all [[of] the]|the)] %*entitydatas% (within|[with]in radius) %number% [(block[s]|met(er|re)[s])] (of|around) %location%",
				"[(all [[of] the]|the)] entities of type[s] %entitydatas% in radius %number% (of|around) %location%");
	}

	@SuppressWarnings("null")
	Expression<? extends EntityData<?>> types;

	@Nullable
	private Expression<World> worlds;
	@Nullable
	private Expression<Chunk> chunks;
	@Nullable
	private Expression<Number> radius;
	@Nullable
	private Expression<Location> center;

	private Class<? extends Entity> returnType = Entity.class;
	private boolean isUsingRadius;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		types = (Expression<? extends EntityData<?>>) exprs[0];
		if (matchedPattern % 2 == 0) {
			for (EntityData<?> d : ((Literal<EntityData<?>>) types).getAll()) {
				if (d.isPlural().isFalse() || d.isPlural().isUnknown() && !StringUtils.startsWithIgnoreCase(parseResult.expr, "all"))
					return false;
			}
		}
		isUsingRadius = matchedPattern >= 2;
		if (isUsingRadius) {
			radius = (Expression<Number>) exprs[exprs.length - 2];
			center = (Expression<Location>) exprs[exprs.length - 1];
		} else {
			if (parseResult.mark == 1) {
				chunks = (Expression<Chunk>) exprs[2];
			} else {
				worlds = (Expression<World>) exprs[1];
			}
		}
		if (types instanceof Literal && ((Literal<EntityData<?>>) types).getAll().length == 1)
			returnType = ((Literal<EntityData<?>>) types).getSingle().getType();
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean isLoopOf(String s) {
		if (!(types instanceof Literal<?>))
			return false;
		try (LogHandler ignored = new BlockingLogHandler().start()) {
			EntityData<?> d = EntityData.parseWithoutIndefiniteArticle(s);
			if (d != null) {
				for (EntityData<?> t : ((Literal<EntityData<?>>) types).getAll()) {
					assert t != null;
					if (!d.isSupertypeOf(t))
						return false;
				}
				return true;
			}
		}
		return false;
	}

	@Override
	@Nullable
	@SuppressWarnings("null")
	protected Entity[] get(Event e) {
		if (isUsingRadius) {
			Iterator<? extends Entity> iter = iterator(e);
			if (iter == null || !iter.hasNext())
				return null;

			List<Entity> l = new ArrayList<>();
			while (iter.hasNext())
				l.add(iter.next());
			return l.toArray((Entity[]) Array.newInstance(returnType, l.size()));
		} else {
			if (chunks != null) {
				return EntityData.getAll(types.getArray(e), returnType, chunks.getArray(e));
			} else {
				return EntityData.getAll(types.getAll(e), returnType, worlds != null ? worlds.getArray(e) : null);
			}
		}
	}

	@Override
	@Nullable
	@SuppressWarnings("null")
	public Iterator<? extends Entity> iterator(Event e) {
		if (isUsingRadius) {
			assert center != null;
			Location l = center.getSingle(e);
			if (l == null)
				return null;
			assert radius != null;
			Number n = radius.getSingle(e);
			if (n == null)
				return null;
			double d = n.doubleValue();

			if (l.getWorld() == null) // safety
				return null;

			Collection<Entity> es = l.getWorld().getNearbyEntities(l, d, d, d);
			double radiusSquared = d * d * Skript.EPSILON_MULT;
			EntityData<?>[] ts = types.getAll(e);
			return new CheckedIterator<>(es.iterator(), e1 -> {
					if (e1 == null || e1.getLocation().distanceSquared(l) > radiusSquared)
						return false;
					for (EntityData<?> t : ts) {
						if (t.isInstance(e1))
							return true;
					}
					return false;
				});
		} else {
			if (chunks == null || returnType == Player.class)
				return super.iterator(e);

			return Arrays.stream(EntityData.getAll(types.getArray(e), returnType, chunks.getArray(e))).iterator();
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return returnType;
	}

	@Override
	@SuppressWarnings("null")
	public String toString(@Nullable Event e, boolean debug) {
		return "all entities of type " + types.toString(e, debug) + (worlds != null ? " in " + worlds.toString(e, debug) :
				radius != null && center != null ? " in radius " + radius.toString(e, debug) + " around " + center.toString(e, debug) : "");
	}

}
