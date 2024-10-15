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

import java.util.Iterator;
import java.util.Objects;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
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

@Name("Affected Entities")
@Description("The affected entities in the <a href='events.html#aoe_cloud_effect'>area cloud effect</a> event.")
@Examples({"on area cloud effect:",
		"\tloop affected entities:",
		"\t\tif loop-value is a player:",
		"\t\t\tsend \"WARNING: you've step on an area effect cloud!\" to loop-value"})
@Since("2.4")
public class ExprAffectedEntities extends SimpleExpression<LivingEntity> {

	static {
		Skript.registerExpression(ExprAffectedEntities.class, LivingEntity.class, ExpressionType.SIMPLE, "[the] affected entities");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		if (!getParser().isCurrentEvent(AreaEffectCloudApplyEvent.class)) {
			Skript.error("The 'affected entities' expression may only be used in an area cloud effect event.");
			return false;
		}
		return true;
	}

	@Override
	protected LivingEntity @Nullable [] get(Event event) {
		if (event instanceof AreaEffectCloudApplyEvent areaEvent)
			return areaEvent.getAffectedEntities().toArray(new LivingEntity[0]);
		return null;
	}

	@Override
	public @Nullable Iterator<? extends LivingEntity> iterator(Event event) {
		if (event instanceof AreaEffectCloudApplyEvent areaEvent)
			return areaEvent.getAffectedEntities().iterator();
		return super.iterator(event);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, SET, DELETE, REMOVE -> CollectionUtils.array(LivingEntity[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof AreaEffectCloudApplyEvent areaEvent))
			return;

		LivingEntity[] entities = (LivingEntity[]) delta;
		switch (mode) {
			case REMOVE:
				for (LivingEntity entity : entities) {
					areaEvent.getAffectedEntities().remove(entity);
				}
				break;
			case SET:
				areaEvent.getAffectedEntities().clear();
				// FALLTHROUGH
			case ADD:
				for (LivingEntity entity : entities) {
					areaEvent.getAffectedEntities().add(entity);
				}
				break;
			case RESET, DELETE:
				areaEvent.getAffectedEntities().clear();
				break;
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public boolean isDefault() {
		return true;
	}

	@Override
	public Class<? extends LivingEntity> getReturnType() {
		return LivingEntity.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the affected entities";
	}

}
