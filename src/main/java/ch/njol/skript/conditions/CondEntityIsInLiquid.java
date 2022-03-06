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
package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;

@Name("Entity is in Liquid")
@Description("Checks whether an entity is in rain, lava, water or a bubble column.")
@Examples({"if player is in rain:",
		"if player is in water:",
		"player is in lava:",
		"player is in bubble column"})
@RequiredPlugins("Minecraft 1.16+ (in water), Paper 1.16+ (in rain, lava and bubble column)")
@Since("2.6.1")
public class CondEntityIsInLiquid extends PropertyCondition<Entity> {
	
	static {
		StringBuilder patterns = new StringBuilder();
		if (Skript.methodExists(Entity.class, "isInWater")) {
			patterns.append("1¦water");
			if (Skript.methodExists(Entity.class, "isInLava")) // Paper - All added at the same time + isInWater
				patterns.append("|2¦lava|3¦[a] bubble[ ]column|4¦rain");
			register(CondEntityIsInLiquid.class, PropertyType.BE, "in (" + patterns + ")", "entities");
		}
	}

	private static final int IN_WATER = 1, IN_LAVA = 2, IN_BUBBLE_COLUMN = 3, IN_RAIN = 4;

	private int mark;

	@Override
	@SuppressWarnings({"unchecked"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<? extends Entity>) exprs[0]);
		setNegated(matchedPattern == 1);
		mark = parseResult.mark;
		return true;
	}
	
	@Override
	public boolean check(Entity entity) {
		switch (mark) {
			case IN_WATER:
				return entity.isInWater();
			case IN_LAVA:
				return entity.isInLava();
			case IN_BUBBLE_COLUMN:
				return entity.isInBubbleColumn();
			case IN_RAIN:
				return entity.isInRain();
			default:
				throw new IllegalStateException();
		}
	}

	@Override
	protected String getPropertyName() {
		switch (mark) {
			case IN_WATER:
				return "in water";
			case IN_LAVA:
				return "in lava";
			case IN_BUBBLE_COLUMN:
				return "in bubble column";
			case IN_RAIN:
				return "in rain";
			default:
				throw new IllegalStateException();
		}
	}

}
