package org.skriptlang.skript.bukkit.item.book.elements.expressions;

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
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.item.book.BookUtils;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Name("Book Pages")
@Description("""
	The pages of a written or writable book.
	Note: In order to modify the pages of a new written book, the book must have a title and author set. \
	Skript will handle this manually, but if you want those to be something else, you must set them.
	""")
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
			.addPatterns("[all [[of] the]|the] [book] (pages|content) of %itemstacks%",
				"%itemstacks%'[s] [book] (pages|content)",
				"[book] page %integer% of %itemstacks%",
				"%itemstacks%'[s] [book] page %integer%")
			.build());
	}

	private Expression<ItemStack> books;
	private @Nullable Expression<Integer> pageNumber;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0 || matchedPattern == 1) {
			books = (Expression<ItemStack>) expressions[0];
		} else if (matchedPattern == 2) {
			pageNumber = (Expression<Integer>) expressions[0];
			books = (Expression<ItemStack>) expressions[1];
		} else {
			books = (Expression<ItemStack>) expressions[0];
			pageNumber = (Expression<Integer>) expressions[1];
		}
		return true;
	}

	@Override
	protected Component[] get(Event event) {
		List<Component> allPages = new ArrayList<>();
		for (ItemStack book : books.getArray(event)) {
			List<? extends Component> pages = BookUtils.getPages(book);
			if (isAllPages()) {
				allPages.addAll(pages);
				continue;
			}
			Integer pageNumber = this.pageNumber.getSingle(event);
			if (pageNumber == null) {
				continue;
			}
			if (pageNumber <= 0 || pageNumber > pages.size()) {
				continue;
			}
			allPages.add(pages.get(pageNumber - 1));
		}
		return allPages.toArray(new Component[0]);
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
		int pageNumber = -1;
		if (!isAllPages()) {
			pageNumber = this.pageNumber.getOptionalSingle(event).orElse(-1);
			if (pageNumber <= 0) {
				return;
			}
		}
		List<Component> newPages = delta == null ? List.of() : new ArrayList<>(delta.length);
		if (delta != null) {
			for (Object page : delta) {
				newPages.add((Component) page);
			}
		}

		for (ItemStack book : books.getArray(event)) {
			if (isAllPages()) {
				switch (mode) {
					case SET, DELETE, RESET -> BookUtils.setPages(book, newPages);
					case ADD -> {
						List<Component> pages = new ArrayList<>(BookUtils.getPages(book));
						pages.addAll(newPages);
						BookUtils.setPages(book, pages);
					}
					default -> throw new IllegalStateException();
				}
			} else {
				List<Component> pages = new ArrayList<>(BookUtils.getPages(book));
				switch (mode) {
					case SET -> {
						if (pageNumber > pages.size()) {
							pages.addAll(Collections.nCopies(pageNumber - pages.size(), Component.empty()));
						}
						pages.set(pageNumber - 1, newPages.getFirst());
					}
					case DELETE -> {
						if (pageNumber > pages.size()) {
							break;
						}
						pages.remove(pageNumber - 1);
					}
					case RESET -> {
						if (pageNumber > pages.size()) {
							continue;
						}
						pages.set(pageNumber - 1, Component.empty());
					}
					default -> throw new IllegalStateException();
				}
				BookUtils.setPages(book, pages);
			}
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
