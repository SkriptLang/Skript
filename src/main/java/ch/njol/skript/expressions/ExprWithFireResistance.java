package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

@Name("With Fire Resistance")
@Description({
	"Creates a copy of an item with (or without) fire resistance."
})
@Examples({
	"set {_x} to diamond sword with fire resistance",
	"equip player with netherite helmet without fire resistance",
	"drop fire resistant stone at player"
})
@RequiredPlugins("Spigot 1.20.5+")
@Since("2.9.0")
public class ExprWithFireResistance extends PropertyExpression<ItemType, ItemType> {

	static {
		if (Skript.methodExists(ItemMeta.class, "setFireResistant", boolean.class))
			Skript.registerExpression(ExprWithFireResistance.class, ItemType.class, ExpressionType.PROPERTY,
				"%itemtype% with[:out] fire[ ]resistance",
				"fire resistant %itemtype%");
	}

	private boolean out;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<ItemType>) exprs[0]);
		out = parseResult.hasTag("out");
		return true;
	}

	@Override
	protected ItemType[] get(Event event, ItemType[] source) {
		return get(source.clone(), item -> {
			ItemMeta meta = item.getItemMeta();
			meta.setFireResistant(!out);
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
		return getExpr().toString(event, debug) + " with fire resistance";
	}

}
