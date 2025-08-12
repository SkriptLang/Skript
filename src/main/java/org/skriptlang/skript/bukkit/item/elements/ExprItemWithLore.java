package org.skriptlang.skript.bukkit.item.elements;

import java.util.List;

import ch.njol.skript.doc.Example;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Item with Lore")
@Description({
	"Returns a copy of an item with with new lore.",
	"If passing multiple components, each with be a line of lore."
})
@Example("""
	set {_item} to stone with lore "line 1" and "line 2"
	give {_item} to player
	""")
@Since("2.3")
public class ExprItemWithLore extends PropertyExpression<ItemType, ItemType> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprItemWithLore.class, ItemType.class)
			.supplier(ExprItemWithLore::new)
			.priority(DEFAULT_PRIORITY)
			.addPattern("%itemtype% with [a|the] lore %textcomponents%")
			.build());
	}

	private Expression<Component> lore;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, ParseResult parseResult) {
		setExpr((Expression<ItemType>) exprs[0]);
		lore = (Expression<Component>) exprs[1];
		return true;
	}

	@Override
	protected ItemType[] get(Event event, ItemType[] source) {
		List<Component> lore = List.of(this.lore.getArray(event));
		return get(source, item -> {
			item = item.clone();
			ItemMeta meta = item.getItemMeta();
			meta.lore(lore);
			item.setItemMeta(meta);
			return item;
		});
	}

	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return getExpr().toString(event, debug) + " with lore " + lore.toString(event, debug);
	}

}
