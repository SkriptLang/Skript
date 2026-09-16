package org.skriptlang.skript.bukkit.item.book;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

@ApiStatus.Internal
public final class BookUtils {

	@SuppressWarnings("ConstantValue") // true on 26.1 and older
	private static final boolean EXTENDS_ADVENTURE_BOOK = Book.class.isAssignableFrom(BookMeta.class);

	private BookUtils() {}

	public static @Unmodifiable List<Component> getPages(BookMeta bookMeta) {
		if (EXTENDS_ADVENTURE_BOOK) {
			//noinspection ConstantConditions
			return ((Book) (Object) bookMeta).pages();
		}
		return bookMeta.pages();
	}

	public static void setPages(BookMeta bookMeta, List<Component> pages) {
		if (EXTENDS_ADVENTURE_BOOK) {
			//noinspection ConstantConditions, ResultOfMethodCallIgnored - modifies in place despite contract
			((Book) (Object) bookMeta).pages(pages);
		} else {
			bookMeta.pages(pages);
		}
	}

	/**
	 * If the title and author of the meta are not set, Minecraft will not update it, as it deems the book
	 * "not signed". This fills in defaults for whichever of the two is missing.
	 */
	public static void signIfNeeded(BookMeta bookMeta) {
		if (!bookMeta.hasTitle()) {
			Component title = bookMeta.hasDisplayName() ? bookMeta.displayName() : Component.text("Written Book");
			bookMeta.title(title);
		}
		if (!bookMeta.hasAuthor()) {
			bookMeta.author(Component.text("Unknown"));
		}
	}

}
