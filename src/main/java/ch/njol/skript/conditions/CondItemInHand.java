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
import ch.njol.skript.aliases.ItemType;
import org.skriptlang.skript.lang.comparator.Relation;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.skriptlang.skript.lang.comparator.Comparators;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.EntityEquipment;
import org.eclipse.jdt.annotation.Nullable;

@Name("Is Holding")
@Description("Checks whether a player is holding a specific item. Cannot be used with endermen, use 'entity is [not] an enderman holding &lt;item type&gt;' instead.")
@Examples({
		"player is holding a stick",
		"victim isn't holding a sword of sharpness"
})
@Since("1.0")
public class CondItemInHand extends Condition {
	
	static {
		Skript.registerCondition(CondItemInHand.class,
				"[%livingentities%] ha(s|ve) %itemtypes% in [main] hand",
				"[%livingentities%] (is|are) holding %itemtypes% [in main hand]",
				"[%livingentities%] ha(s|ve) %itemtypes% in off[(-| )]hand",
				"[%livingentities%] (is|are) holding %itemtypes% in off[(-| )]hand",
				"[%livingentities%] (ha(s|ve) not|do[es]n't have) %itemtypes% in [main] hand",
				"[%livingentities%] (is not|isn't) holding %itemtypes% [in main hand]",
				"[%livingentities%] (ha(s|ve) not|do[es]n't have) %itemtypes% in off[(-| )]hand",
				"[%livingentities%] (is not|isn't) holding %itemtypes% in off[(-| )]hand"
		);
	}
	
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<LivingEntity> entities;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> items;

	private boolean offTool;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		items = (Expression<ItemType>) exprs[1];
		offTool = (matchedPattern == 2 || matchedPattern == 3 || matchedPattern == 6 || matchedPattern == 7);
		setNegated(matchedPattern >= 4);
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		return entities.check(e,
				livingEntity -> items.check(e,
						itemType -> {
							EntityEquipment equipment = livingEntity.getEquipment();
							if (equipment == null)
								return false; // No equipment -> no item in hand
							ItemType handItem = new ItemType(offTool ? equipment.getItemInOffHand() : equipment.getItemInMainHand());
							return Comparators.compare(handItem, itemType).isImpliedBy(Relation.EQUAL);
						}), isNegated());
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return entities.toString(e, debug) + " " + (entities.isSingle() ? "is" : "are")
				+ " holding " + items.toString(e, debug)
				+ (offTool ? " in off-hand" : "");
	}
	
}
