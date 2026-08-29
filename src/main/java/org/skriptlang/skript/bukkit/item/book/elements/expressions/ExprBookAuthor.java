package org.skriptlang.skript.bukkit.item.book.elements.expressions;

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

/**
 * @deprecated This is being removed in favor of {@link org.skriptlang.skript.common.properties.elements.expressions.PropExprAuthor}
 */
@Name("Book Author")
@Description("The author of a book.")
@Example("""
	on book sign:
		broadcast "A new book has been created by %author of event-item%"
	""")
@Since("2.2-dev31")
@Deprecated(since = "INSERT VERSION", forRemoval = true)
public class ExprBookAuthor extends SimplePropertyExpression<ItemType, Component> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		if (SkriptConfig.useTypeProperties.value())
			return;

		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprBookAuthor.class, Component.class,
			"[book] (author|writer|publisher)", "itemtypes", false)
				.supplier(ExprBookAuthor::new)
				.build());
	}

	@Override
	public @Nullable Component convert(ItemType item) {
		if (item.getItemMeta() instanceof BookMeta bookMeta && bookMeta.hasAuthor()) {
			return bookMeta.author();
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
		Component author = delta == null ? null : (Component) delta[0];
		for (ItemType item : getExpr().getArray(event)) {
			if (item.getItemMeta() instanceof BookMeta bookMeta) {
				bookMeta.author(author);
				item.setItemMeta(bookMeta);
			}
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
