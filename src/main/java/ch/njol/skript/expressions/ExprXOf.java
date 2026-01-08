package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("X of Item/Entity Type")
@Description("An expression for using an item or entity type with a different amount.")
@Example("give level of player of iron pickaxes to the player")
@Since("1.2")
@Keywords("amount")
public class ExprXOf extends PropertyExpression<Object, Object> {

	static {
		Skript.registerExpression(ExprXOf.class, Object.class, ExpressionType.PATTERN_MATCHES_EVERYTHING,
			"%number% of %itemstacks/itemtypes/entitytype%");
	}

	private Class<?>[] possibleReturnTypes;
	private Expression<Number> amount;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		amount = (Expression<Number>) exprs[0];
		Expression<?> type = exprs[1];
		setExpr(type);

		// "x of y" is also an ItemType syntax
		if (amount instanceof Literal && amount.getSource() instanceof Literal &&
				type instanceof Literal && type.getSource() instanceof Literal) {
			return false;
		}

		// build possible return types
		List<Class<?>> possibleReturnTypes = new ArrayList<>();
		if (type.canReturn(ItemStack.class)) {
			possibleReturnTypes.add(ItemStack.class);
		}
		if (type.canReturn(ItemType.class)) {
			possibleReturnTypes.add(ItemType.class);
		}
		if (type.canReturn(EntityType.class)) {
			possibleReturnTypes.add(EntityType.class);
		}
		this.possibleReturnTypes = possibleReturnTypes.toArray(new Class[0]);

		return true;
	}

	@Override
	protected Object[] get(Event event, Object[] source) {
		Number amount = this.amount.getSingle(event);
		if (amount == null)
			return (Object[]) Array.newInstance(getReturnType(), 0);

		return get(source, object -> {
			if (object instanceof ItemStack itemStack) {
				itemStack = itemStack.clone();
				itemStack.setAmount(amount.intValue());
				return itemStack;
			} else if (object instanceof ItemType itemType) {
				ItemType type = itemType.clone();
				type.setAmount(amount.intValue());
				return type;
			} else {
				EntityType entityType = ((EntityType) object).clone();
				entityType.amount = amount.intValue();
				return entityType;
			}
		});
	}

	@Override
	public Class<?> getReturnType() {
		return possibleReturnTypes.length == 1 ? possibleReturnTypes[0] : Object.class;
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		return Arrays.copyOf(possibleReturnTypes, possibleReturnTypes.length);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return amount.toString(event, debug) + " of " + getExpr().toString(event, debug);
	}

	@Override
	public Expression<?> simplify() {
		if (amount instanceof Literal && getExpr() instanceof Literal) {
			return SimplifiedLiteral.fromExpression(this);
		}
		return super.simplify();
	}

}
