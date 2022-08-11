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
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
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
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("Book Pages")
@Description("The pages of a book.")
@Examples({
	"on book sign:",
	"\tmessage \"Book Pages: %pages of event-item%\"",
	"\tmessage \"Book Page 1: %page 1 of event-item%\"",
	"set page 1 of player's held item to \"Book writing\""
})
@Since("2.2-dev31, INSERT VERSION (changers)")
public class ExprBookPages extends SimpleExpression<String> {

	private static final ItemType bookItem = Aliases.javaItemType("book with text");

	static {
		Skript.registerExpression(ExprBookPages.class, String.class, ExpressionType.PROPERTY,
			"[all] [the] [book] (pages|content) of %itemtypes%",
			"%itemtypes%'[s] [book] (pages|content)",
			"[book] page %number% of %itemtypes%",
			"%itemtypes%'[s] [book] page %number%");
	}

	@SuppressWarnings("null")
	private Expression<ItemType> book;
	@Nullable
	private Expression<Number> page;

	@SuppressWarnings("null")
	@Nullable
	@Override
	protected String[] get(Event e) {
		ItemStack itemStack = book.getSingle(e).getRandom();
		if (itemStack == null || !bookItem.isOfType(itemStack))
			return null;
		List<String> pages = ((BookMeta) itemStack.getItemMeta()).getPages();
		if (page != null) {
			Number pageNumber = page.getSingle(e);
			if (pageNumber == null)
				return null;
			int page = pageNumber.intValue();
			if ((page) > pages.size() || page < 1)
				return null;
			return new String[]{pages.get(page - 1)};
		} else {
			return pages.toArray(new String[pages.size()]);
		}
	}

	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case RESET:
			case DELETE:
				return CollectionUtils.array(page == null ? String[].class : String.class);
			case ADD:
				return page == null ? CollectionUtils.array(String[].class) : null;
			default:
				return null;
		}
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		List<ItemType> itemTypes = new ArrayList<>(Arrays.asList(book.getArray(e)));
		Number pageNumber = page == null ? -1 : page.getSingle(e);
		if (pageNumber == null) return;
		int page = pageNumber.intValue();
		String[] newPages = delta == null ? null : new String[delta.length];
		if (newPages != null)
			for (int i = 0; i < delta.length; i++)
				newPages[i] = delta[i] + "";
		for (ItemType itemType : itemTypes) {
			if (itemType == null) return;
			ItemMeta meta = itemType.getItemMeta();
			if (!(meta instanceof BookMeta)) return;
			BookMeta bookMeta = (BookMeta) meta;
			List<String> currentPages = new ArrayList<>(bookMeta.getPages());
			int pageCount = bookMeta.getPageCount();
			switch (mode) {
				case DELETE:
				case RESET:
					if (this.page == null) {
						currentPages = Arrays.asList("");
					} else {
						if (page <= 0 || page > pageCount)
							return;
						currentPages.remove(page - 1);
					}
					break;
				case SET:
					if (newPages.length == 0) return;
					if (this.page == null) {
						currentPages = Arrays.asList(newPages);
					} else {
						if (page <= 0)
							return;
						while (currentPages.size() < page) {
							currentPages.add("");
						}
						currentPages.set(page - 1, newPages[0]);
					}
					break;
				case ADD:
					currentPages.addAll(Arrays.asList(newPages));
					break;
			}
			bookMeta.setPages(currentPages);
			itemType.setItemMeta(bookMeta);
		}
	}

	@Override
	public boolean isSingle() {
		return page != null;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "book pages of " + book.toString(e, debug);
	}

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (matchedPattern == 0 || matchedPattern == 1) {
			book = (Expression<ItemType>) exprs[0];
		} else {
			if (matchedPattern == 2) {
				page = (Expression<Number>) exprs[0];
				book = (Expression<ItemType>) exprs[1];
			} else {
				book = (Expression<ItemType>) exprs[0];
				page = (Expression<Number>) exprs[1];
			}
		}
		return true;
	}
}
