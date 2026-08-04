package org.skriptlang.skript.bukkit.item.book;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.WritableBookContent;
import io.papermc.paper.datacomponent.item.WrittenBookContent;
import io.papermc.paper.text.Filtered;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public final class BookUtils {

	private BookUtils() { }

	public static WrittenBookContent modifyWrittenContent(WrittenBookContent content, Consumer<WrittenBookContent.Builder> modifier) {
		return modifyWrittenContent(content, modifier, true);
	}

	public static WrittenBookContent modifyWrittenContent(WrittenBookContent content, Consumer<WrittenBookContent.Builder> modifier, boolean preservePages) {
		WrittenBookContent.Builder builder = WrittenBookContent.writtenBookContent(content.title(), content.author())
			.generation(content.generation())
			.resolved(content.resolved());
		if (preservePages) {
			//noinspection unchecked, rawtypes
			builder.addFilteredPages((List) content.pages());
		}
		modifier.accept(builder);
		return builder.build();
	}

	public static List<? extends Component> getPages(ItemStack book) {
		if (book.hasData(DataComponentTypes.WRITTEN_BOOK_CONTENT)) {
			//noinspection ConstantConditions - checked via hasData
			return book.getData(DataComponentTypes.WRITTEN_BOOK_CONTENT).pages().stream()
				.map(Filtered::raw)
				.toList();
		}
		if (book.hasData(DataComponentTypes.WRITABLE_BOOK_CONTENT)) {
			//noinspection ConstantConditions - checked via hasData
			return book.getData(DataComponentTypes.WRITABLE_BOOK_CONTENT).pages().stream()
				.map(filtered -> Component.text(filtered.raw()))
				.toList();
		}
		return List.of();
	}

	public static void setPages(ItemStack book, List<? extends Component> pages) {
		boolean hasWrittenBookContent = book.hasData(DataComponentTypes.WRITTEN_BOOK_CONTENT);
		if (hasWrittenBookContent || book.getType() == Material.WRITTEN_BOOK) {
			WrittenBookContent newContent;
			if (hasWrittenBookContent) {
				//noinspection ConstantConditions - checked via hasData
				newContent = modifyWrittenContent(book.getData(DataComponentTypes.WRITTEN_BOOK_CONTENT),
					content -> content.addPages(pages), false);
			} else {
				newContent = WrittenBookContent.writtenBookContent(getDefaultTitle(book), getDefaultAuthor(book))
					.addPages(pages)
					.build();
			}
			book.setData(DataComponentTypes.WRITTEN_BOOK_CONTENT, newContent);
			return;
		}

		if (book.hasData(DataComponentTypes.WRITABLE_BOOK_CONTENT) || book.getType() == Material.WRITABLE_BOOK) {
			LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
			List<String> stringPages = pages.stream()
				.map(serializer::serialize)
				.toList();
			book.setData(DataComponentTypes.WRITABLE_BOOK_CONTENT, WritableBookContent.writeableBookContent()
				.addPages(stringPages)
				.build());
		}
	}

	public static String getDefaultTitle(ItemStack book) {
		return LegacyComponentSerializer.legacySection().serialize(book.effectiveName());
	}

	public static String getDefaultAuthor(ItemStack book) {
		return "Unknown";
	}

	@SuppressWarnings("ConstantValue") // true on 26.1 and older
	private static final boolean EXTENDS_ADVENTURE_BOOK = Book.class.isAssignableFrom(BookMeta.class);

	public static @Unmodifiable List<Component> getPages(BookMeta bookMeta) {
		if (EXTENDS_ADVENTURE_BOOK) {
			//noinspection ConstantConditions
			return ((Book) (Object) bookMeta).pages();
		}
		return bookMeta.pages();
	}

}
