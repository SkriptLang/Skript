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
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Fall Distance")
@Description({"The distance an entity has fallen for."})
@Examples({"set all entities' fall distance to 10",
	"on damage:",
	"\tsend \"%victim's fall distance%\" to victim"})
@Since("2.5")
public class ExprFallDistance extends SimplePropertyExpression<Entity, Number> {
	
	static {
		register(ExprFallDistance.class, Number.class, "fall[en] (distance|height)", "entities");
	}
	
	@Nullable
	@Override
	public Number convert(Entity entity) {
		return entity.getFallDistance();
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		return (mode == ChangeMode.RESET || mode == ChangeMode.REMOVE_ALL || mode == ChangeMode.DELETE) ? null : CollectionUtils.array(Number.class);
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta != null) {
			Entity[] entities = getExpr().getArray(e);
			if (entities.length < 1)
				return;
			Float number = ((Number) delta[0]).floatValue();
			for (Entity entity : entities) {
				
				Float fallDistance = entity.getFallDistance();
				
				switch (mode) {
					case ADD:
						entity.setFallDistance(fallDistance + number);
						break;
					case SET:
						entity.setFallDistance(number);
						break;
					case REMOVE:
						entity.setFallDistance(fallDistance - number);
						break;
					default:
						assert false;
				}
			}
		}
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "fall distance";
	}
	
}
