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
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.ExpressionType;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Name("Loot Table From Key")
@Description("Returns the loot table from a key.")
@Examples("loot table from key \"minecraft:chests/simple_dungeon\"")
@Since("INSERT VERSION")
public class ExprLootTableFromString extends SimplePropertyExpression<String, LootTable> {

	public static final Pattern KEY_PATTERN = Pattern.compile("([a-z0-9._-]+:)?([a-z0-9/._-]+)");

	static {
		Skript.registerExpression(ExprLootTableFromString.class, LootTable.class, ExpressionType.COMBINED,
			"loot[ ]table[s] (from|of) [[the] key[s]] %strings%"
		);
	}

	@Override
	public @Nullable LootTable convert(String string) {
		String lowerCase = string.toLowerCase(Locale.ENGLISH);
		NamespacedKey key;
		Matcher keyMatcher = KEY_PATTERN.matcher(lowerCase);
		if (!keyMatcher.matches())
			return null;
		try {
			String namespace = keyMatcher.group(1);
			String keyValue = keyMatcher.group(2);
			if (namespace == null) {
				key = NamespacedKey.minecraft(keyValue);
			} else {
				namespace = namespace.substring(0, namespace.length() - 1);
				key = new NamespacedKey(namespace, keyValue);
			}
		} catch (IllegalArgumentException argument) {
			return null;
		}
		return Bukkit.getLootTable(key);
	}

	@Override
	public Class<? extends LootTable> getReturnType() {
		return LootTable.class;
	}

	@Override
	protected String getPropertyName() {
		assert false;
		return null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "loot table from keys " + getExpr().toString(event, debug);
	}
}
