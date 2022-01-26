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

@Name("Gliding State")
@Description("Sets of gets gliding state of player. It allows you to set gliding state of entity even if they do not have an <a href=\"https://minecraft.gamepedia.com/Elytra\">Elytra</a> equipped.")
@Examples({"set gliding of player to off"})
@Since("2.2-dev21")
public class ExprGlidingState extends SimplePropertyExpression<LivingEntity, Boolean> {

	static {
		if (Skript.isRunningMinecraft(1, 9))
			register(ExprGlidingState.class, Boolean.class, "(gliding|glider) [state]", "livingentities");
	}

	@Override
	public Boolean convert(final LivingEntity e) {
		return e.isGliding();
	}

	@Override
	protected String getPropertyName() {
		return "gliding state";
	}

	@Override
	public Class<Boolean> getReturnType() {
		return Boolean.class;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET)
			return new Class[] {Boolean.class};
		return null;
	}

	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
		for (final LivingEntity entity : getExpr().getArray(e))
			entity.setGliding(delta == null ? false : (Boolean) delta[0]);
	}
}
