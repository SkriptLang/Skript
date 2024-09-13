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

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.aliases.ItemType;
import org.skriptlang.skript.lang.comparator.Relation;
import ch.njol.skript.classes.data.DefaultComparators;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;

@Name("Is of Type")
@Description("Checks whether an item or an entity is of the given type. This is mostly useful for variables," +
		" as you can use the general 'is' condition otherwise (e.g. 'victim is a creeper').")
@Examples({"tool is of type {selected type}",
		"victim is of type {villager type}"})
@Since("1.4")
public class CondIsOfType extends Condition {
	
	static {
		PropertyCondition.register(CondIsOfType.class, "of type[s] %itemtypes/entitydatas%", "itemstacks/entities");
	}
	
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> what;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> types;
	
	@SuppressWarnings("null")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		what = exprs[0];
		types = exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(Event event) {
		return what.check(event,
				(Checker<Object>) o1 -> types.check(event,
						(Checker<Object>) o2 -> {
							if (o2 instanceof ItemType && o1 instanceof ItemStack) {
								return ((ItemType) o2).isSupertypeOf(new ItemType((ItemStack) o1));
							} else if (o2 instanceof EntityData && o1 instanceof Entity) {
								return ((EntityData<?>) o2).isInstance((Entity) o1);
							} else if (o2 instanceof ItemType && o1 instanceof Entity) {
								return Relation.EQUAL.isImpliedBy(DefaultComparators.entityItemComparator.compare(EntityData.fromEntity((Entity) o1), (ItemType) o2));
							} else {
								return false;
							}
						}),
				isNegated());
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.BE, event, debug, what,
				"of " + (types.isSingle() ? "type " : "types ") + types.toString(event, debug));
	}
	
}
