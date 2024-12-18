package ch.njol.skript.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.ServerPlatform;
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
import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;

@Name("Book Pages")
@Description({
	"The pages of a book (Supports Skript's chat format)",
	"Note: In order to modify the pages of a new written book, you must have the title and author",
	"of the book set. Skript will do this for you, but if you want your own, please set those values."
})
@Examples({
	"on book sign:",
		"\tmessage \"Book Pages: %pages of event-item%\"",
		"\tmessage \"Book Page 1: %page 1 of event-item%\"",
	"",
	"set page 1 of player's held item to \"Book writing\""
})
@Since("2.2-dev31, 2.7 (changers)")
public class ExprBookPages extends SimpleExpression<String> {

	private static BungeeComponentSerializer serializer;

	static {
		Skript.registerExpression(ExprBookPages.class, String.class, ExpressionType.PROPERTY,
				"[all [[of] the]|the] [book] (pages|content) of %itemtypes/itemstacks%",
				"%itemtypes/itemstacks%'[s] [book] (pages|content)",
				"[book] page %number% of %itemtypes/itemstacks%",
				"%itemtypes/itemstacks%'[s] [book] page %number%"
		);
		if (Skript.isRunningMinecraft(1, 16) && Skript.getServerPlatform() == ServerPlatform.BUKKIT_PAPER)
			serializer = BungeeComponentSerializer.get();
	}

	private Expression<?> items;

	@Nullable
	private Expression<Number> page;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0 || matchedPattern == 1) {
			items = exprs[0];
		} else if (matchedPattern == 2) {
			page = (Expression<Number>) exprs[0];
			items = exprs[1];
		} else {
			items = exprs[0];
			page = (Expression<Number>) exprs[1];
		}
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		List<String> pages = new ArrayList<>();
		for (Object object : items.getArray(event)) {
			BookMeta bookMeta = null;
			if (object instanceof ItemType) {
				ItemType itemType = (ItemType) object;
				if (!(itemType.getItemMeta() instanceof BookMeta))
					continue;
				bookMeta = (BookMeta) itemType.getItemMeta();
			} else if (object instanceof ItemStack) {
				ItemStack itemstack = (ItemStack) object;
				if (!(itemstack.getItemMeta() instanceof BookMeta))
					continue;
				bookMeta = (BookMeta) itemstack.getItemMeta();
			} else {
				assert false;
			}
			if (bookMeta == null)
				continue;
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
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta == null && (mode == ChangeMode.SET || mode == ChangeMode.ADD))
			return;
		int page = !isAllPages() ? this.page.getOptionalSingle(event).orElse(-1).intValue() : -1;
		String[] newPages = delta == null ? null : new String[delta.length];
		if (newPages != null) {
			for (int i = 0; i < delta.length; i++)
				newPages[i] = delta[i] + "";
		}

		for (Object object : items.getArray(event)) {
			BookMeta bookMeta = null;
			if (object instanceof ItemType) {
				ItemType itemType = (ItemType) object;
				if (!(itemType.getItemMeta() instanceof BookMeta))
					continue;
				bookMeta = (BookMeta) itemType.getItemMeta();
			} else if (object instanceof ItemStack) {
				ItemStack itemstack = (ItemStack) object;
				if (!(itemstack.getItemMeta() instanceof BookMeta))
					continue;
				bookMeta = (BookMeta) itemstack.getItemMeta();
			} else {
				assert false;
			}
			if (bookMeta == null)
				continue;

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
					case ADD:
						pages = new ArrayList<>(bookMeta.getPages());
						pages.addAll(Arrays.asList(newPages));
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
				default:
					break;
			}
			if (serializer != null) {
				if (!bookMeta.hasTitle() && bookMeta.hasDisplayName())
					bookMeta.title(bookMeta.displayName());
				List<Component> components = pages.stream()
						.map(ChatMessages::parseToArray)
						.map(BungeeConverter::convert)
						.map(serializer::deserialize)
						.collect(Collectors.toList());
				bookMeta.pages(components);
			} else {
				bookMeta.setPages(pages);
			}
			// If the title and author of the bookMeta are not set, Minecraft will not update the BookMeta, as it deems the book "not signed".
			if (!bookMeta.hasTitle()) {
				String title = bookMeta.hasDisplayName() ? bookMeta.getDisplayName() : "Written Book";
				bookMeta.setTitle(title);
			}
			if (!bookMeta.hasAuthor())
				bookMeta.setAuthor("Server");
			if (object instanceof ItemType) {
				((ItemType) object).setItemMeta(bookMeta);
			} else if (object instanceof ItemStack) {
				((ItemStack) object).setItemMeta(bookMeta);
			}
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
		return (page != null ? "page " + page.toString(event, debug) : "book pages") + " of " + items.toString(event, debug);
	}

}
