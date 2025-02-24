package org.skriptlang.skript.bukkit.tags.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.Tag;

import java.util.Objects;

public class LitAnyItem extends SimpleLiteral<ItemType> {

	static {
		Skript.registerExpression(LitAnyItem.class, ItemType.class, ExpressionType.SIMPLE,
				"[an[y]] item [tagged as %-minecrafttag%]",
				"all items [tagged as %-minecrafttag%]");
	}

	public LitAnyItem() {
		super(CollectionUtils.array(new ItemType(ItemData.wildcard())), ItemType.class, true);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		Expression<?> tagExpression = expressions[0];
		if (tagExpression !=  null) {
			if (!(tagExpression.simplify() instanceof Literal)) {
				Skript.error("The tag in an any/all item expression must be a literal.");
				return false;
			}
			// filter by tag
			Tag<?> tag = (Tag<?>) ((Literal<?>) tagExpression.simplify()).getSingle();
			Material[] values = tag.getValues().stream()
				.filter(Objects::nonNull)
				.filter(Material.class::isInstance)
				.toArray(Material[]::new);
			if (values.length == 0) {
				Skript.error("The tag in an any/all item expression must be a tag of materials.");
				return false;
			}
			this.data[0] = new ItemType(values);
		}
		if (matchedPattern == 1)
			this.data[0].setAll(true);
		return true;
	}
}
