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
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Creeper Max Fuse Duration")
@Description("The maximum fuse duration that a creeper has.")
@Examples("set target entity's max fuse time to 1 second")
@Since("INSERT VERSION")
public class ExprCreeperMaxFuseDuration extends SimplePropertyExpression<LivingEntity, Timespan> {
	
	static {
		if (Skript.methodExists(Creeper.class, "getMaxFuseTicks"))
			register(ExprCreeperMaxFuseDuration.class, Timespan.class, "[creeper] max[imum] fuse (time|duration)", "livingentities");
	}
	
	@Override
	public Timespan convert(LivingEntity entity) {
		return Timespan.fromTicks_i(entity instanceof Creeper ? ((Creeper) entity).getMaxFuseTicks() : 0);
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return (mode != ChangeMode.REMOVE_ALL) ? CollectionUtils.array(Timespan.class) : null;
	}
	
	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		LivingEntity[] entities = getExpr().getArray(event);
		int amount = delta == null ? 0 : (int) ((Timespan) delta[0]).getTicks_i();
		for (LivingEntity entity : entities) {
			if (!(entity instanceof Creeper))
				continue;
			Creeper creeper = (Creeper) entity;
			switch (mode) {
				case REMOVE:
					amount = -amount;
				case ADD:
					creeper.setMaxFuseTicks(Math.max(creeper.getMaxFuseTicks() + amount, 0));
					break;
				case RESET:
					creeper.setMaxFuseTicks(30);
					break;
				case DELETE:
				case SET:
					creeper.setMaxFuseTicks(Math.max(amount, 0));
					break;
				default:
					assert false;
			}
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "creeper max fuse time";
	}
	
}
