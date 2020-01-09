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
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.expressions;

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
import ch.njol.skript.lang.ExpressionType;
import ch.njol.util.coll.CollectionUtils;

@Name("Arrows Stuck")
@Description("The number of arrows stuck in a living entity.")
@Examples("set arrows stuck in player to 5")
@Since("INSERT VERSION")
public class ExprArrowsStuck extends SimplePropertyExpression<LivingEntity, Number> {

    static {
    	if (Skript.methodExists(LivingEntity.class, "getArrowsStuck")) {
    		Skript.registerExpression(ExprArrowsStuck.class, Number.class, ExpressionType.PROPERTY,
    				"[number of] arrow[s] stuck in %livingentities%");
    	}
    }

    @Override
    public Number convert(LivingEntity le) {
        return le.getArrowsStuck();
    }
    
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.REMOVE || mode == ChangeMode.REMOVE_ALL)
			return null;
		return CollectionUtils.array(Number.class);
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		int d = delta == null ? 0 : ((Number) delta[0]).intValue();
		for (LivingEntity le : getExpr().getArray(e)) {
			switch (mode) {
				case ADD:
					le.setArrowsStuck(le.getArrowsStuck() + d);
					break;
				case SET:
					le.setArrowsStuck(d);
					break;
				case DELETE:
				case RESET:
					le.setArrowsStuck(0);
					break;
				case REMOVE:
				case REMOVE_ALL:
					assert false;		
			}
		}
	}
	
    @Override
    protected String getPropertyName() {
        return "arrows stuck";
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

}
