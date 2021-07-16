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

import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Creeper Max Fuse Ticks")
@Description("The max fuse ticks that a creeper has.")
@Examples("set target entity's max fuse ticks to 20 #1 second")
@Since("2.5")
public class ExprCreeperMaxFuseTicks extends SimplePropertyExpression<LivingEntity, Long> {
	
	static {
		if(Skript.methodExists(LivingEntity.class, "getMaxFuseTicks"))
			register(ExprCreeperMaxFuseTicks.class, Long.class, "[creeper] max[imum] fuse tick[s]", "livingentities");
	}

	@Override
	public Long convert(LivingEntity e) {
		return e instanceof Creeper ? (long) ((Creeper) e).getMaxFuseTicks() : 0;
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL)
			return null;
		return CollectionUtils.array(Number.class);
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		int d = delta == null ? 0 : ((Number) delta[0]).intValue();
		for (LivingEntity le : getExpr().getArray(e)) {
			if (le instanceof Creeper) {
				Creeper c = (Creeper) le;
				switch (mode) {
					case ADD:
						int r1 = c.getMaxFuseTicks() + d;
						if (r1 < 0) r1 = 0;
						c.setMaxFuseTicks(r1);
						break;
					case SET:
						c.setMaxFuseTicks(d);
						break;
					case DELETE:
						c.setMaxFuseTicks(0);
						break;
					case RESET:
						c.setMaxFuseTicks(30); //Seems to be the same for powered creepers?
						break;
					case REMOVE:
						int r2 = c.getMaxFuseTicks() - d;
						if (r2 < 0) r2 = 0;
						c.setMaxFuseTicks(r2);
						break;
					case REMOVE_ALL:
						assert false;		
				}
			}
		}
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	protected String getPropertyName() {
		return "creeper max fuse ticks";
	}
	
}
