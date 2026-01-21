package org.skriptlang.skript.bukkit.item.book;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Name("Book Pages")
@Description({
	"The pages of a book (Supports Skript's chat format)",
	"Note: In order to modify the pages of a new written book, you must have the title and author",
	"of the book set. Skript will do this for you, but if you want your own, please set those values."
})
@Example("""
	on book sign:
		if the number of pages of event-item is greater than 1:
			message "The second page of the authored book is: %page 2 of event-item%"
	""")
@Example("set page 1 of the player's held item to \"This page was written with Skript!\"")
@Since("2.2-dev31, 2.7 (changers)")
public class ExprBookPages extends SimpleExpression<Component> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprBookPages.class, Component.class)
			.supplier(ExprBookPages::new)
			.priority(PropertyExpression.DEFAULT_PRIORITY)
			.addPatterns("[all [[of] the]|the] [book] (pages|content) of %itemtypes%",
				"%itemtypes%'[s] [book] (pages|content)",
				"[book] page %integer% of %itemtypes%",
				"%itemtypes%'[s] [book] page %integer%")
			.build());
	}

	private Expression<ItemType> books;
	private @Nullable Expression<Integer> pageNumber;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0 || matchedPattern == 1) {
			books = (Expression<ItemType>) expressions[0];
		} else if (matchedPattern == 2) {
			pageNumber = (Expression<Integer>) expressions[0];
			books = (Expression<ItemType>) expressions[1];
		} else {
			books = (Expression<ItemType>) expressions[0];
			pageNumber = (Expression<Integer>) expressions[1];
		}
		return true;
	}

	@Override
	protected Component[] get(Event event) {
		List<Component> pages = new ArrayList<>();
		for (ItemType book : books.getArray(event)) {
			if (book.getMaterial() != Material.WRITTEN_BOOK || !(book.getItemMeta() instanceof BookMeta bookMeta)) {
				return new Component[0];
			}
			if (isAllPages()) {
				pages.addAll(bookMeta.pages());
			} else {
				Integer pageNumber = this.pageNumber.getSingle(event);
				if (pageNumber == null) {
					continue;
				}
				if (pageNumber <= 0 || pageNumber > bookMeta.getPageCount()) {
					continue;
				}
				pages.add(bookMeta.page(pageNumber));
			}
		}
		return pages.toArray(new Component[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET -> CollectionUtils.array(isAllPages() ? Component[].class : Component.class);
			case ADD -> isAllPages() ? CollectionUtils.array(Component[].class) : null;
			case DELETE, RESET -> CollectionUtils.array();
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int pageNumber = isAllPages() ? -1 : this.pageNumber.getOptionalSingle(event).orElse(-1);
		List<Component> newPages = delta == null ? Collections.emptyList() : new ArrayList<>(delta.length);
		if (delta != null) {
			for (Object page : delta) {
				newPages.add((Component) page);
			}
			if (pageNumber != -1 && newPages.isEmpty()) { // not setting this page to anything, exit early
				return;
			}
		}

		for (ItemType book : books.getArray(event)) {
			if (book.getMaterial() != Material.WRITTEN_BOOK || !(book.getItemMeta() instanceof BookMeta bookMeta)) {
				continue;
			}

			if (isAllPages()) {
				switch (mode) {
					case SET, DELETE, RESET -> //noinspection ResultOfMethodCallIgnored - modifies in place despite contract
						bookMeta.pages(newPages);
					case ADD -> bookMeta.addPages(newPages.toArray(new Component[0]));
					default -> throw new IllegalStateException();
				}
			} else {
				switch (mode) {
					case SET -> bookMeta.page(pageNumber, newPages.getFirst());
					case DELETE -> {
						List<Component> pages = new ArrayList<>(bookMeta.pages());
						pages.remove(pageNumber);
						//noinspection ResultOfMethodCallIgnored - modifies in place despite contract
						bookMeta.pages(pages);
					}
					case RESET -> bookMeta.page(pageNumber, Component.empty());
					default -> throw new IllegalStateException();
				}
			}

			// if the title and author of the bookMeta are not set, Minecraft will not update the BookMeta, as it deems the book "not signed".
			if (!bookMeta.hasTitle()) {
				Component title = bookMeta.hasDisplayName() ? bookMeta.displayName() : Component.text("Written Book");
				bookMeta.title(title);
			}
			if (!bookMeta.hasAuthor()) {
				bookMeta.author(Component.text("Unknown"));
			}

			// update book
			book.setItemMeta(bookMeta);
		}
	}

	private boolean isAllPages() {
		return pageNumber == null;
	}

	@Override
	public boolean isSingle() {
		return books.isSingle() && !isAllPages();
	}

	@Override
	public Class<? extends Component> getReturnType() {
		return Component.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (isAllPages()) {
			builder.append("all of the pages");
		} else {
			builder.append("page", pageNumber);
		}
		builder.append("of", books);
		return builder.toString();
	}

}
