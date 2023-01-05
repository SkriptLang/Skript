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

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.ExprName;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@Name("Is Named")
@Description("Checks whether or not a item, block, slot, inventory, player or entity is named or has a given name")
@Examples({
	"on right click with a cake:",
	"\tif event-item is named \"&bBirthday Cake\"",
	"\t\tsend \"Happy Birthday, %player's name%!\"",
	"",
	"on damage of a player:",
	"\tattacker's tool is named",
	"\tcancel event",
	"",
	"on inventory click:",
	"\tevent-inventory is named \"Example Inventory\":",
	"\t\tcancel event",
	""
})
@Since("INSERT VERSION")
public class CondIsNamed extends Condition {

	private static final ExprName expression = new ExprName();
	private Expression<Object> objects;
	@Nullable
	private Expression<String> name;

	static {
		PropertyCondition.register(CondIsNamed.class, PropertyType.BE, "named [%-string%]", "offlineplayers/entities/blocks/itemtypes/inventories/slots/worlds");
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		objects = (Expression<Object>) exprs[0];
		name = (Expression<String>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		String name = this.name != null ? this.name.getSingle(event) : null;
		return objects.check(event, object -> {
			String value = expression.convert(object);
			return name != null ? value.equalsIgnoreCase(name) : value != null;
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.BE, event, debug, objects,
			"named" + (name == null ? "" : " " + name.toString(event, debug)));
	}

}
