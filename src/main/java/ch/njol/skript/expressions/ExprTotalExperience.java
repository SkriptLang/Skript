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

import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Total Experience")
@Description({
	"The total experience, in points, of players or experience orbs.",
	"Adding to a player's experience will trigger Mending, but setting their experience will not."
})
@Examples({
	"set total experience of player to 100",
	"",
	"add 100 to player's experience",
	"",
	"if player's total experience is greater than 100:",
	"\tset player's total experience to 0",
	"\tgive player 1 diamond"
})
@Since("2.7")
public class ExprTotalExperience extends SimplePropertyExpression<Entity, Integer> {

	static {
		register(ExprTotalExperience.class, Integer.class, "[total] experience", "entities");
	}

	@Override
	@Nullable
	public Integer convert(Entity entity) {
		// experience orbs
		if (entity instanceof ExperienceOrb)
			return ((ExperienceOrb) entity).getExperience();

		// players need special treatment
		if (entity instanceof Player)
			return PlayerUtils.getTotalXP(((Player) entity).getLevel(), ((Player) entity).getExp());

		// invalid entity type
		return null;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case ADD:
			case REMOVE:
			case SET:
			case DELETE:
			case RESET:
				return new Class[]{Number.class};
			case REMOVE_ALL:
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		int change = delta == null ? 0 : ((Number) delta[0]).intValue();
		switch (mode) {
			case RESET:
			case DELETE:
				// RESET and DELETE will have change = 0, so just fall through to SET
			case SET:
				if (change < 0)
					change = 0;
				for (Entity entity : getExpr().getArray(event)) {
					if (entity instanceof ExperienceOrb) {
						((ExperienceOrb) entity).setExperience(change);
					} else if (entity instanceof Player) {
						PlayerUtils.setTotalXP((Player) entity, change);
					}
				}
				break;
			case REMOVE:
				change = -change;
				// fall through to ADD
			case ADD:
				int xp;
				for (Entity entity : getExpr().getArray(event)) {
					if (entity instanceof ExperienceOrb) {
						//ensure we don't go below 0
						xp = ((ExperienceOrb) entity).getExperience() + change;
						((ExperienceOrb) entity).setExperience(Math.max(xp, 0));
					} else if (entity instanceof Player) {
						// can only giveExp() positive experience
						if (change < 0) {
							// ensure we don't go below 0
							xp = PlayerUtils.getTotalXP((Player) entity) + change;
							PlayerUtils.setTotalXP((Player) entity, (Math.max(xp, 0)));
						} else {
							((Player) entity).giveExp(change);
						}
					}
				}
				break;
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "total experience";
	}
}
