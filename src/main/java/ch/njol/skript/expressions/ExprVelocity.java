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
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Sashie
 */
@Name("Vectors - Velocity")
@Description("Gets or changes velocity of an entity.")
@Examples({"set player's velocity to {_v}"})
@Since("2.2-dev31")
public class ExprVelocity extends SimplePropertyExpression<Entity, Vector> {

	static {
		register(ExprVelocity.class, Vector.class, "velocit(y|ies)", "entities");
	}

	@Override
	@Nullable
	public Vector convert(Entity e) {
		return e.getVelocity();
	}

	@Override
	@Nullable
	@SuppressWarnings("null")
	public Class<?>[] acceptChange(ChangeMode mode) {
		if ((mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.SET || mode == ChangeMode.DELETE || mode == ChangeMode.RESET))
			return CollectionUtils.array(Vector.class);
		return null;
	}

	@Override
	@SuppressWarnings("null")
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		assert mode == ChangeMode.DELETE || mode == ChangeMode.RESET || delta != null;
		for (final Entity entity : getExpr().getArray(e)) {
			if (entity == null)
				return;
			switch (mode) {
				case ADD:
					entity.setVelocity(entity.getVelocity().add((Vector) delta[0]));
					break;
				case REMOVE:
					entity.setVelocity(entity.getVelocity().subtract((Vector) delta[0]));
					break;
				case REMOVE_ALL:
					break;
				case DELETE:
				case RESET:
					entity.setVelocity(new Vector());
					break;	
				case SET:
					entity.setVelocity((Vector) delta[0]);
			}
		}
	}

	@Override
	protected String getPropertyName() {
		return "velocity";
	}

	@Override
	public Class<Vector> getReturnType() {
		return Vector.class;
	}

}
