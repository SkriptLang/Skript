package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.common.AnyAmount;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.properties.expressions.PropExprAmount;

/**
 * @deprecated This is being removed in favor of {@link PropExprAmount}
 */
@Name("Amount")
@Description({"The amount or size of something.",
		"Please note that <code>amount of %items%</code> will not return the number of items, but the number of stacks, e.g. 1 for a stack of 64 torches. To get the amount of items in a stack, see the <a href='#ExprItemAmount'>item amount</a> expression."
})
@Example("message \"There are %number of all players% players online!\"")
@Since("1.0")
@Deprecated(since="2.13", forRemoval = true)
public class ExprAmount extends SimpleExpression<Number> {

	static {
		if (!SkriptConfig.useTypeProperties.value())
			Skript.registerExpression(ExprAmount.class, Number.class, ExpressionType.PROPERTY,
					"[the] (amount|number|size) of %numbered%",
					"[the] (amount|number|size) of %objects%");
	}

	@SuppressWarnings("null")
	private ExpressionList<?> exprs;
	private @Nullable Expression<AnyAmount> any;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0) {
			//noinspection unchecked
			this.any = (Expression<AnyAmount>) exprs[0];
			return true;
		}

		this.exprs = exprs[0] instanceof ExpressionList<?> exprList
				? exprList
				: new ExpressionList<>(new Expression<?>[]{ exprs[0] }, Object.class, false);

		this.exprs = (ExpressionList<?>) LiteralUtils.defendExpression(this.exprs);
		if (!LiteralUtils.canInitSafely(this.exprs)) {
			return false;
		}

		if (this.exprs.isSingle()) {
			Skript.error("'" + this.exprs.toString(null, Skript.debug()) + "' can only ever have one value at most, thus the 'amount of ...' expression is useless. Use '... exists' instead to find out whether the expression has a value.");
			return false;
		}

		return true;
	}

	@Override
	protected Number[] get(Event event) {
		if (any != null)
			return new Number[] {any.getOptionalSingle(event).orElse(() -> 0).amount()};
		return new Long[]{(long) exprs.getArray(event).length};
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		if (any != null) {
			return switch (mode) {
				case SET, ADD, RESET, DELETE, REMOVE -> CollectionUtils.array(Number.class);
				default -> null;
			};
		}
		return super.acceptChange(mode);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (any == null) {
			super.change(event, delta, mode);
			return;
		}
		double amount = delta != null ? ((Number) delta[0]).doubleValue() : 1;
		// It's okay to treat it as a double even if it's a whole number because there's no case in
		// the set of real numbers where (x->double + y->double)->long != (x+y)
		switch (mode) {
			case REMOVE:
				amount = -amount;
				//$FALL-THROUGH$
			case ADD:
				for (AnyAmount obj : any.getArray(event)) {
					if (obj.supportsAmountChange())
						obj.setAmount(obj.amount().doubleValue() + amount);
				}
				break;
			case RESET, DELETE, SET:
				for (AnyAmount any : any.getArray(event)) {
					if (any.supportsAmountChange())
						any.setAmount(amount);
				}
				break;
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return any != null ? Number.class : Long.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (any != null)
			return "amount of " + any.toString(event, debug);
		return "amount of " + exprs.toString(event, debug);
	}

}
