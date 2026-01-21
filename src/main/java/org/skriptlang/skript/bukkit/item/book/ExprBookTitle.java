package org.skriptlang.skript.bukkit.item.book;

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

@Name("Book Title")
@Description("The title of a book.")
@Example("""
	on book sign:
		message "You finished your book titled %title of event-item%"
	""")
@Since("2.2-dev31")
public class ExprBookTitle extends SimplePropertyExpression<ItemType, Component> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprBookTitle.class, Component.class,
			"book (name|title)", "itemtypes", false)
				.supplier(ExprBookTitle::new)
				.build());
	}

	@Override
	public Component convert(ItemType item) {
		if (item.getItemMeta() instanceof BookMeta bookMeta && bookMeta.hasTitle()) {
			return bookMeta.title();
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(Component.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Component title = delta == null ? null : (Component) delta[0];
		for (ItemType item : getExpr().getArray(event)) {
			if (item.getItemMeta() instanceof BookMeta bookMeta) {
				bookMeta.title(title);
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
		return "book title";
	}

}
