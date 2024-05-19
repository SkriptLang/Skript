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

import ch.njol.skript.classes.Changer;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Collidable State")
@Description("Gets/sets the collidable state of a living entity.")
@Examples({
	"set collidable of player to false"
})
@Since("INSERT VERSION")
public class ExprCollidableState extends SimplePropertyExpression<LivingEntity, Boolean> {

	static {
		register(ExprCollidableState.class, Boolean.class, "collidable [state|mode]", "livingentities");
	}

	@Override
	public Boolean convert(LivingEntity entity) {
		return entity.isCollidable();
	}

	@Override
	protected String getPropertyName() {
		return "collidable state";
	}

	@Override
	public Class<Boolean> getReturnType() {
		return Boolean.class;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		switch (mode){
			case SET:
				return CollectionUtils.array(Boolean.class);
			case RESET:
				return CollectionUtils.array();
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		getExpr().stream(event).forEach(entity ->
			entity.setCollidable(mode == ChangeMode.RESET | (delta != null && (Boolean) delta[0]))
		);
	}

}
