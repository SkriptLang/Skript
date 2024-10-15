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
import ch.njol.skript.util.Experience;
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
	"\tgive player 1 diamond",
	"",
	"on level progress change:",
	"\tset {_changed.xp} to experience of event-experience"
})
@Since("2.7, INSERT_VERSION (experience point support)")
public class ExprTotalExperience extends SimplePropertyExpression<Object, Integer> {

	static {
		register(ExprTotalExperience.class, Integer.class, "[total] experience", "entities/experiences");
	}

	@Override
	@Nullable
	public Integer convert(Object object) {
		// experience orbs
		if (object instanceof ExperienceOrb experienceOrb)
			return experienceOrb.getExperience();

		// players need special treatment
		if (object instanceof Player player)
			return PlayerUtils.getTotalXP(player.getLevel(), player.getExp());

		// experience points
		if (object instanceof Experience experience) {
			return experience.getXP();
		}

		// invalid type
		return null;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, REMOVE, SET, DELETE, RESET -> new Class[]{Number.class};
			default -> null;
		};
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
				for (Object object : getExpr().getArray(event)) {
					if (object instanceof ExperienceOrb experienceOrb) {
						experienceOrb.setExperience(change);
					} else if (object instanceof Player player) {
						PlayerUtils.setTotalXP(player, change);
					} else if (object instanceof Experience experience) {
						experience.setXP(change);
					}
				}
				break;
			case REMOVE:
				change = -change;
				// fall through to ADD
			case ADD:
				int xp;
				for (Object object : getExpr().getArray(event)) {
					if (object instanceof ExperienceOrb experienceOrb) {
						// ensure we don't go below 0
						xp = experienceOrb.getExperience() + change;
						experienceOrb.setExperience(Math.max(xp, 0));
					} else if (object instanceof Player player) {
						// can only giveExp() positive experience
						if (change < 0) {
							// ensure we don't go below 0
							xp = PlayerUtils.getTotalXP(player) + change;
							PlayerUtils.setTotalXP(player, Math.max(xp, 0));
						} else {
							player.giveExp(change);
						}
					} else if (object instanceof Experience experience) {
						xp = experience.getXP() + change;
						experience.setXP(Math.max(xp, 0));
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
