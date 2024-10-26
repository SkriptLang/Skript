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
import org.jetbrains.annotations.Nullable;

@Name("Book Author")
@Description("The author of a book.")
@Examples({
	"on book sign:",
		"\tmessage \"Book Title: %author of event-item%\""
})
@Since("2.2-dev31")
public class ExprBookAuthor extends SimplePropertyExpression<ItemType, String> {

	static {
		register(ExprBookAuthor.class, String.class, "[book] (author|writer|publisher)", "itemtypes");
	}

	@Nullable
	@Override
	public String convert(ItemType item) {
		return item.getItemMeta() instanceof BookMeta bookMeta ? bookMeta.getAuthor() : null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, RESET, DELETE -> CollectionUtils.array(String.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		String author = delta == null ? null : (String) delta[0];
		for (ItemType item : getExpr().getArray(event)) {
			if (item.getItemMeta() instanceof BookMeta bookMeta) {
				bookMeta.setAuthor(author);
				item.setItemMeta(bookMeta);
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
