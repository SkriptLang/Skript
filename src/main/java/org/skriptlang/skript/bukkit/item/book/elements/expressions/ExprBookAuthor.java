package org.skriptlang.skript.bukkit.item.book.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.WrittenBookContent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.item.book.BookUtils;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Book Author")
@Description("The author of a book.")
@Example("""
	on book sign:
		broadcast "A new book has been created by %author of event-item%"
	""")
@Since("2.2-dev31")
public class ExprBookAuthor extends SimplePropertyExpression<ItemStack, Component> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprBookAuthor.class, Component.class,
			"[book] (author|writer|publisher)", "itemstacks", false)
				.supplier(ExprBookAuthor::new)
				.build());
	}

	@Override
	public @Nullable Component convert(ItemStack book) {
		if (book.hasData(DataComponentTypes.WRITTEN_BOOK_CONTENT)) {
			//noinspection ConstantConditions - checked via hasData
			return LegacyComponentSerializer.legacySection()
				.deserialize(book.getData(DataComponentTypes.WRITTEN_BOOK_CONTENT).author());
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, RESET, DELETE -> CollectionUtils.array(Component.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		String author = delta == null ? "" : LegacyComponentSerializer.legacySection().serialize(((Component) delta[0]));
		for (ItemStack book : getExpr().getArray(event)) {
			boolean hasContent = book.hasData(DataComponentTypes.WRITTEN_BOOK_CONTENT);
			if (!hasContent && book.getType() != Material.WRITTEN_BOOK) {
				continue;
			}
			WrittenBookContent newContent;
			if (hasContent) {
				newContent = BookUtils.modifyWrittenContent(book.getData(DataComponentTypes.WRITTEN_BOOK_CONTENT),
					content -> content.author(author));
			} else {
				newContent = WrittenBookContent.writtenBookContent(BookUtils.getDefaultTitle(book), author).build();
			}
			book.setData(DataComponentTypes.WRITTEN_BOOK_CONTENT, newContent);
		}
	}

	@Override
	public Class<? extends Component> getReturnType() {
		return Component.class;
	}

	@Override
	protected String getPropertyName() {
		return "book author";
	}

}
