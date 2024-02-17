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

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.visual.VisualEffect;
import ch.njol.skript.util.visual.VisualEffectType;
import ch.njol.skript.util.visual.VisualEffects;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

// Registration done in VisualEffects#generateTypes
public class ExprVisualEffect extends SimpleExpression<VisualEffect> {

	@SuppressWarnings("NotNullFieldNotInitialized")
	private VisualEffectType type;

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?>[] data;

	@Nullable
	private Expression<Number> speed;
	@Nullable
	private Expression<Number> dX, dY, dZ;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		type = VisualEffects.get(matchedPattern);

		data = new Expression[exprs.length - 4];
		System.arraycopy(exprs, 0, data, 0, exprs.length - 4);

		if ((parseResult.mark & 1) != 0) {
			dX = (Expression<Number>) exprs[exprs.length - 4];
			dY = (Expression<Number>) exprs[exprs.length - 3];
			dZ = (Expression<Number>) exprs[exprs.length - 2];
		}

		if ((parseResult.mark & 2) != 0) {
			speed = (Expression<Number>) exprs[exprs.length - 1];
		}

		return true;
	}

	@Nullable
	@Override
	protected VisualEffect[] get(Event e) {
		Object[] data = new Object[this.data.length];
		for (int i = 0; i < data.length; i++) {
			Expression<?> expr = this.data[i];
			if (expr != null) {
				data[i] = expr.isSingle() ? expr.getSingle(e) : expr.getArray(e);
			}
		}

		float speed = 0F;
		if (this.speed != null) {
			Number speedNumber = this.speed.getSingle(e);
			if (speedNumber != null)
				speed = speedNumber.floatValue();
		}

		float dX = 0F;
		if (this.dX != null) {
			Number dXNumber = this.dX.getSingle(e);
			if (dXNumber != null)
				dX = dXNumber.floatValue();
		}
		float dY = 0F;
		if (this.dY != null) {
			Number dYNumber = this.dY.getSingle(e);
			if (dYNumber != null)
				dY = dYNumber.floatValue();
		}
		float dZ = 0F;
		if (this.dZ != null) {
			Number dZNumber = this.dZ.getSingle(e);
			if (dZNumber != null)
				dZ = dZNumber.floatValue();
		}

		VisualEffect visualEffect = new VisualEffect(type, data, speed, dX, dY, dZ);
		return new VisualEffect[] {visualEffect};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends VisualEffect> getReturnType() {
		return VisualEffect.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "visual effect " + type.getName();
	}

}
