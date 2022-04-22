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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.eclipse.jdt.annotation.Nullable;

@Name("Custom Chest Inventory")
@Description("Returns a chest inventory with the given amount of rows and the name. Use the <a href=effects.html#EffOpenInventory>open inventory</a> effect to open it.")
@Examples({"open chest inventory with 1 row named \"test\" to player",
	"set {_inventory} to chest inventory with 1 row"})
@Since("2.2-dev34")
public class ExprChestInventory extends SimpleExpression<Inventory> {

	static {
		Skript.registerExpression(ExprChestInventory.class, Inventory.class, ExpressionType.COMBINED,
			"[a [new]] chest inventory (named|with name) %string% [with %-number% row[s]]",
			"[a [new]] chest inventory with %number% row[s] [(named|with name) %-string%]");
	}

	private static final String DEFAULT_CHEST_TITLE = InventoryType.CHEST.getDefaultTitle();
	private static final int DEFAULT_CHEST_ROWS = InventoryType.CHEST.getDefaultSize() / 9;

	@Nullable
	private Expression<Number> rows;
	@Nullable
	private Expression<String> name;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		name = (Expression<String>) exprs[matchedPattern];
		rows = (Expression<Number>) exprs[matchedPattern ^ 1];
		return true;
	}

	@Override
	protected Inventory[] get(Event e) {
		String name = this.name != null ? this.name.getSingle(e) : DEFAULT_CHEST_TITLE;
		Number rows = this.rows != null ? this.rows.getSingle(e) : DEFAULT_CHEST_ROWS;

		rows = rows == null ? DEFAULT_CHEST_ROWS : rows;
		name = name == null ? DEFAULT_CHEST_TITLE : name;

		int size = rows.intValue() * 9;
		if (size % 9 != 0) {
			size = 27;
		}

		// Sanitize inventory size
		if (size < 0)
			size = 0;
		if (size > 54) // Too big values cause visual weirdness, or exceptions on newer server versions
			size = 54;

		return CollectionUtils.array(Bukkit.createInventory(null, size, name));
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Inventory> getReturnType() {
		return Inventory.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "chest inventory named "
			+ (name != null ? name.toString(e, debug) : "\"" + DEFAULT_CHEST_TITLE + "\"") +
			" with "
			+ (rows != null ? rows.toString(e, debug) : "" + DEFAULT_CHEST_ROWS + " rows");
	}

}
