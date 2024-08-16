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

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;

@Name("Custom Chest Inventory")
@Description("Returns a chest inventory with the given amount of rows and the name. Use the <a href=effects.html#EffOpenInventory>open inventory</a> effect to open it.")
@Examples({
	"open chest inventory with 1 row named \"test\" to player",
	"",
	"set {_inventory} to a chest inventory with 1 row",
	"set slot 4 of {_inventory} to a diamond named \"example\"",
	"open {_inventory} to player",
	"",
	"open chest inventory named \"<#00ff00>hex coloured title!\" with 6 rows to player",
})
@RequiredPlugins("Paper 1.16+ (chat format)")
@Since("2.2-dev34, 2.8.0 (chat format)")
public class ExprChestInventory extends SimpleExpression<Inventory> {

	@Nullable
	private static BungeeComponentSerializer serializer;

	static {
		if (Skript.classExists("net.kyori.adventure.text.Component") &&
				Skript.methodExists(Bukkit.class, "createInventory", InventoryHolder.class, int.class, Component.class))
			serializer = BungeeComponentSerializer.get();
		Skript.registerExpression(ExprChestInventory.class, Inventory.class, ExpressionType.COMBINED,
				"[a] [new] chest inventory (named|with name) %string% [with %-number% row[s]]",
				"[a] [new] chest inventory with %number% row[s] [(named|with name) %-string%]");
	}

	private static final String DEFAULT_CHEST_TITLE = InventoryType.CHEST.getDefaultTitle();
	private static final int DEFAULT_CHEST_ROWS = InventoryType.CHEST.getDefaultSize() / 9;

	@Nullable
	private Expression<Number> rows;

	@Nullable
	private Expression<String> name;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		name = (Expression<String>) exprs[matchedPattern];
		rows = (Expression<Number>) exprs[matchedPattern ^ 1];
		return true;
	}

	@Override
	protected Inventory[] get(Event event) {
		String name = this.name != null ? this.name.getOptionalSingle(event).orElse(DEFAULT_CHEST_TITLE) : DEFAULT_CHEST_TITLE;
		Number rows = this.rows != null ? this.rows.getOptionalSingle(event).orElse(DEFAULT_CHEST_ROWS) : DEFAULT_CHEST_ROWS;

		int size = rows.intValue() * 9;
		if (size % 9 != 0)
			size = 27;

		// Sanitize inventory size
		if (size < 0)
			size = 0;
		if (size > 54) // Too big values cause visual weirdness, or exceptions on newer server versions
			size = 54;

		if (serializer != null) {
			BaseComponent[] components = BungeeConverter.convert(ChatMessages.parseToArray(name));
			return CollectionUtils.array(Bukkit.createInventory(null, size, serializer.deserialize(components)));
		}
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
	public String toString(@Nullable Event event, boolean debug) {
		return "chest inventory named " +
				(name != null ? name.toString(event, debug) : "\"" + DEFAULT_CHEST_TITLE + "\"") +
				" with " + (rows != null ? rows.toString(event, debug) : "" + DEFAULT_CHEST_ROWS + " rows");
	}

}
