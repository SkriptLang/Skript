/**
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
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
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import ch.njol.skript.lang.util.SimpleExpression;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;

@Name("Nearest Entity")
@Description("Gets the entity nearest to a location or another entity.")
@Examples({
	"kill the nearest pig and cow relative to player",
	"teleport player to the nearest cow relative to player",
	"teleport player to the nearest entity relative to player",
	"",
	"on click:",
	"\tkill nearest pig"
})
@Since("2.7")
public class ExprNearestEntity extends SimpleExpression<Entity> {

	static {
		Skript.registerExpression(ExprNearestEntity.class, Entity.class, ExpressionType.COMBINED,
				"[the] nearest %*entitydatas% [[relative] to %entity/location%]",
				"[the] %*entitydatas% nearest [to %entity/location%]");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private EntityData<?>[] entityDatas;

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> relativeTo;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entityDatas = ((Literal<EntityData<?>>) exprs[0]).getArray();
		if (entityDatas.length != Arrays.stream(entityDatas).distinct().count()) {
			Skript.error("Entity list may not contain duplicate entities");
			return false;
		}
		relativeTo = exprs[1];
		return true;
	}

	@Override
	protected Entity[] get(Event event) {
		Object relativeTo = this.relativeTo.getSingle(event);
		if (relativeTo == null || (relativeTo instanceof Location && ((Location) relativeTo).getWorld() == null))
			return new Entity[0];
		Entity[] nearestEntities = new Entity[entityDatas.length];
		for (int i = 0; i < nearestEntities.length; i++) {
			if (relativeTo instanceof Entity) {
				nearestEntities[i] = getNearestEntity(entityDatas[i], ((Entity) relativeTo).getLocation(), (Entity) relativeTo);
			} else {
				nearestEntities[i] = getNearestEntity(entityDatas[i], (Location) relativeTo, null);
			}
		}
		return nearestEntities;
	}

	@Override
	public boolean isSingle() {
		return entityDatas.length == 1;
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return entityDatas.length == 1 ? entityDatas[0].getType() : Entity.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "nearest " + StringUtils.join(entityDatas) + " relative to " + relativeTo.toString(event, debug);
	}

	@Nullable
	private Entity getNearestEntity(EntityData<?> entityData, Location relativePoint, @Nullable Entity excludedEntity) {
		Entity nearestEntity = null;
		double nearestDistance = -1;
		for (Entity entity : relativePoint.getWorld().getEntitiesByClass(entityData.getType())) {
			if (entity != excludedEntity && entityData.isInstance(entity)) {
				double distance = entity.getLocation().distance(relativePoint);
				if (nearestEntity == null || distance < nearestDistance) {
					nearestDistance = distance;
					nearestEntity = entity;
				}
			}
		}
		return nearestEntity;
	}

}
