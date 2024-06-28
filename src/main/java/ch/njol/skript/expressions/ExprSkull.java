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

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Player Skull")
@Description("Gets a skull item representing a player. Skulls for other entities are provided by the aliases.")
@Examples({"give the victim's skull to the attacker",
		"set the block at the entity to the entity's skull"})
@Since("2.0")
public class ExprSkull extends SimplePropertyExpression<OfflinePlayer, ItemType> {
	
	static {
		register(ExprSkull.class, ItemType.class, "(head|skull)", "offlineplayers");
	}
	
	private static final ItemType playerSkull = Aliases.javaItemType("player skull");
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	@Nullable
	public ItemType convert(OfflinePlayer o) {
		ItemType skull = playerSkull.clone();
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		meta.setOwningPlayer(o);
		skull.setItemMeta(meta);
		return skull;
	}
	
	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "skull";
	}
	
}
