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
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("No Damage Duration")
@Description("How much time an entity is invulnerable for.")
@Examples({"on damage:",
		"\tset victim's invulnerability time to 1 second # victim will not take damage for the next second"})
@Since("2.5, INSERT VERSION (Use Timespan)")
public class ExprNoDamageDuration extends SimplePropertyExpression<LivingEntity, Timespan> {
	
	static {
		register(ExprNoDamageDuration.class, Timespan.class, "(invulnerability|no damage) (time|duration)", "livingentities");
	}
	
	@Override
	public Timespan convert(LivingEntity entity) {
		return Timespan.fromTicks_i(Math.max(entity.getNoDamageTicks(), 0));
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
			switch (mode) {
				case REMOVE:
					amount = -amount;
				case ADD:
					entity.setNoDamageTicks(Math.max(entity.getNoDamageTicks() + amount, 0));
					break;
				case RESET:
					entity.setNoDamageTicks(10); // Default value from https://minecraft.fandom.com/wiki/Damage
					break;
				case DELETE:
				case SET:
					entity.setNoDamageTicks(Math.max(amount, 0));
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
		return "no damage time";
	}
	
}
