package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("X of Item")
@Description("An expression to be able to use a certain amount of items where the amount can be any expression. Please note that this expression is not stable and might be replaced in the future.")
@Examples("give level of player of pickaxes to the player")
@Since("1.2")
public class ExprXOf extends PropertyExpression<Object, Object> {

	static {
		Skript.registerExpression(ExprXOf.class, Object.class, ExpressionType.PATTERN_MATCHES_EVERYTHING, "%number% of %itemstacks/itemtypes/entitytype%");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Number> amount;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[1]);
		amount = (Expression<Number>) exprs[0];
		// "x of y" is also an ItemType syntax
		return !(amount instanceof Literal) || !(getExpr() instanceof Literal);
	}

	@Override
	protected Object[] get(Event e, Object[] source) {
		Number a = amount.getSingle(e);
		if (a == null)
			return new Object[0];

		return get(source, o -> {
			if (o instanceof ItemStack) {
				ItemStack is = ((ItemStack) o).clone();
				is.setAmount(a.intValue());
				return is;
			} else if (o instanceof ItemType) {
				ItemType type = ((ItemType) o).clone();
				type.setAmount(a.intValue());
				return type;
			} else {
				EntityType t = ((EntityType) o).clone();
				t.amount = a.intValue();
				return t;
			}
		});
	}

	@Override
	public Class<?> getReturnType() {
		return getExpr().getReturnType();
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return amount.toString(e, debug) + " of " + getExpr().toString(e, debug);
	}

}
