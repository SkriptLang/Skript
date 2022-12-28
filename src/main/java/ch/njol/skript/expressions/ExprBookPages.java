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
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.BookMeta;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

	static {
		Skript.registerExpression(ExprBookPages.class, String.class, ExpressionType.PROPERTY,
			"[all [[of] the]|the] [book] (pages|content) of %itemtypes%",
			"%itemtypes%'[s] [book] (pages|content)",
			"[book] page %number% of %itemtypes%",
			"%itemtypes%'[s] [book] page %number%"
		);
	}

	private Expression<ItemType> items;
	@Nullable
	private Expression<Number> page;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0 || matchedPattern == 1) {
			items = (Expression<ItemType>) exprs[0];
		} else if (matchedPattern == 2) {
			page = (Expression<Number>) exprs[0];
			items = (Expression<ItemType>) exprs[1];
		} else {
			items = (Expression<ItemType>) exprs[0];
			page = (Expression<Number>) exprs[1];
		}
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		List<String> pages = new ArrayList<>();
		for (ItemType itemType : items.getArray(event)) {
			if (!(itemType.getItemMeta() instanceof BookMeta))
				continue;
			BookMeta bookMeta = (BookMeta) itemType.getItemMeta();
			if (isAllPages()) {
				pages.addAll(bookMeta.getPages());
			} else {
				Number pageNumber = page.getSingle(event);
				if (pageNumber == null)
					continue;
				int page = pageNumber.intValue();
				if (page <= 0 || page > bookMeta.getPageCount())
					continue;
				pages.add(bookMeta.getPage(page));
			}
		}
		return pages.toArray(new String[0]);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case RESET:
			case DELETE:
				return CollectionUtils.array();
			case SET:
				return CollectionUtils.array(isAllPages() ? String[].class : String.class);
			case ADD:
				return isAllPages() ? CollectionUtils.array(String[].class) : null;
			default:
				return null;
		}
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta == null && (mode == ChangeMode.SET || mode == ChangeMode.ADD))
			return;
		ItemType[] itemTypes = items.getArray(event);
		int page = !isAllPages() ? this.page.getOptionalSingle(event).orElse(-1).intValue() : -1;
		String[] newPages = delta == null ? null : new String[delta.length];

		if (newPages != null) {
			for (int i = 0; i < delta.length; i++)
				newPages[i] = delta[i] + "";
		}

		for (ItemType itemType : itemTypes) {
			if (!(itemType.getItemMeta() instanceof BookMeta))
				continue;

			BookMeta bookMeta = (BookMeta) itemType.getItemMeta();
			List<String> pages = null;
			if (isAllPages()) {
				switch (mode) {
					case DELETE:
					case RESET:
						pages = Collections.singletonList("");
						break;
					case SET:
						pages = Arrays.asList(newPages);
						break;
					default:
						assert false;
				}
			} else {
				pages = new ArrayList<>(bookMeta.getPages());
			}
			int pageCount = bookMeta.getPageCount();

			switch (mode) {
				case DELETE:
				case RESET:
					if (!isAllPages()) {
						if (page <= 0 || page > pageCount)
							continue;
						pages.remove(page - 1);
					}
					break;
				case SET:
					if (newPages.length == 0)
						continue;
					if (!isAllPages()) {
						if (page <= 0)
							continue;
						while (pages.size() < page)
							pages.add("");
						pages.set(page - 1, newPages[0]);
					}
					break;
				case ADD:
					pages.addAll(Arrays.asList(newPages));
					break;
			}

			bookMeta.setPages(pages);
			itemType.setItemMeta(bookMeta);
		}
	}

	private boolean isAllPages() {
		return page == null;
	}

	@Override
	public boolean isSingle() {
		return items.isSingle() && !isAllPages();
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (page != null ? "page " + page.toString(event, debug) : "the book pages") + " of " + items.toString(event, debug);
	}

}
