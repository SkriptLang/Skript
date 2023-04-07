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

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Love Time")
@Description({
	"The amount of time an living entity has been in love for. Setting to 30 seconds is equal to using breeding item",
	"returns '0 seconds' if null or invalid entity"})
@Examples({
	"on right click:",
	"\tsend \"%event-enttiy% has been in love for %love time of event-entity%!\" to player"
})
@Since("INSERT VERSION")
public class ExprLoveTime extends SimplePropertyExpression<LivingEntity, Timespan> {

	static {
		register(ExprLoveTime.class, Timespan.class, "love time", "livingentities");
	}

	@Override
	@Nullable
	public Timespan convert(LivingEntity livingEntity) {
		int loveTicks = 0;
		if (livingEntity instanceof Animals)
			loveTicks = ((Animals) livingEntity).getLoveModeTicks();
		return Timespan.fromTicks_i(loveTicks);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Timespan.class);
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null)
			return;
		long ticks = ((Timespan) delta[0]).getTicks_i();
		for (LivingEntity livingEntity : getExpr().getArray(event)) {
			if (livingEntity instanceof Animals)
				((Animals) livingEntity).setLoveModeTicks((int) ticks);
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "love time";
	}

}
