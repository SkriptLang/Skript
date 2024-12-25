package ch.njol.skript.expressions;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Item with Lore")
@Description({"Returns the given item type with the specified lore added to it.",
		"If multiple strings are passed, each of them will be a separate line in the lore."})
@Examples({"set {_test} to stone with lore \"line 1\" and \"line 2\"",
		"give {_test} to player"})
@Since("2.3")
public class ExprItemWithLore extends PropertyExpression<ItemType, ItemType> {

	static {
		Skript.registerExpression(ExprItemWithLore.class, ItemType.class, ExpressionType.PROPERTY,
				"%itemtype% with [(a|the)] lore %strings%");
	}

	@SuppressWarnings("null")
	private Expression<String> lore;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, ParseResult parseResult) {
		setExpr((Expression<ItemType>) exprs[0]);
		lore = (Expression<String>) exprs[1];
		return true;
	}

	@Override
	protected ItemType[] get(Event e, ItemType[] source) {
		List<String> lore = this.lore.stream(e)
			.flatMap(l -> Arrays.stream(l.split("\n")))
			.collect(Collectors.toList());

		return get(source, item -> {
			item = item.clone();
			ItemMeta meta = item.getItemMeta();
			meta.setLore(lore);
			item.setItemMeta(meta);
			return item;
		});
	}


	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return getExpr().toString(e, debug) + " with lore " + lore.toString(e, debug);
	}
}
