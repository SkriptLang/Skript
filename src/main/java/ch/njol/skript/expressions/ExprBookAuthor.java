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

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

import org.bukkit.event.Event;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

@Name("Book Author")
@Description("The author of a book.")
@Examples({"on book sign:",
			"\tmessage \"Book Title: %author of event-item%\""})
@Since("2.2-dev31")
public class ExprBookAuthor extends SimplePropertyExpression<ItemType, String> {
	
	static {
		register(ExprBookAuthor.class, String.class, "[book] (author|writer|publisher)", "itemtypes");
	}
	
	@Nullable
	@Override
	public String convert(ItemType item) {
		ItemMeta meta = item.getItemMeta();
		
		if (meta instanceof BookMeta)
			return ((BookMeta) meta).getAuthor();
		
		return null;
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(String.class);
		return null;
	}
	
	@SuppressWarnings("null")
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		String author = delta == null ? null : (String) delta[0];
		
		for (ItemType item : getExpr().getArray(e)) {
			ItemMeta meta = item.getItemMeta();
			
			if (meta instanceof BookMeta) {
				((BookMeta) meta).setAuthor(author);
				item.setItemMeta(meta);
			}
		}
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "book author";
	}
	
}

