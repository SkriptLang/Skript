package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Number of Decimal Places")
@Description("""
	Gets the number of decimal places in a number.
	This expression will limit the number of decimal places to the 'number accuracy' option in the config, which is 2 by default.
	Providing a custom limit will override the default behavior with the provided integer limit.
	Specifying 'with no limit' will get as many decimal places as possible from the provided number(s).
	""")
@Example("""
	# the 'number accuracy' option in the config is 2 by default
	set {_decimalPlaces1} to the number of decimal places of 1.2345
	# {_decimalPlaces} = 2
	set {_decimalPlaces} to the number of decimal places 1.2
	# {_decimalPlaces} = 1
	set {_decimalPlaces2} to the number of decimal places of 1.00000001
	# {_decimalPlaces} = 2
	""")
@Example("""
	set {_decimalPlaces} to the number of decimal places of 1.234567 with a limit of 4
	# {_decimalPlaces} = 4
	set {_decimalPlaces} to the number of decimal places of 1.23456 with a limit of 7
	# {_decimalPlaces} = 5
	set {_decimalPlaces} to the number of decimal places of 1.000000001 with a limit of 6
	# {_decimalPlaces} = 6
	""")
@Example("""
	set {_decimalPlaces} to the number of decimal places of 1.23456789 with no limit
	# {_decimalPlaces} = 8
	set {_decimalPlaces} to the number of decimal places of 1.0000000000001 with no limit
	# {_decimalPlaces} = 13
	""")
@Example("""
	set {_decimalPlaces} to the number of decimal places of 1
	set {_decimalPlaces} to the number of decimal places of 1.00000000000
	set {_decimalPlaces} to the number of decimal places of 1.00000 with a limit of 4
	set {_decimalPlaces} to the number of decimal places of 1.0000000 with no limit
	# {_decimalPlaces} = <none>
	""")
@Since("INSERT VERSION")
public class ExprDecimalPlaces extends SimpleExpression<Long> {

	private static final double EPSILON = 1e-9;

	static {
		Skript.registerExpression(ExprDecimalPlaces.class, Long.class, ExpressionType.COMBINED,
			"[the] number of decimal places of %numbers% [limited:with a (limit|cap) of %-integer%]",
			"[the] number of decimal places of %numbers% with no (limit|cap)");
	}

	private Expression<Number> numbers;
	private @Nullable Expression<Integer> limit;
	private boolean isLimited;
	private boolean isUnlimited;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		numbers = (Expression<Number>) exprs[0];
		isLimited = parseResult.hasTag("limited");
		isUnlimited = matchedPattern == 1;
		if (isLimited) {
			//noinspection unchecked
			limit = (Expression<Integer>) exprs[1];
		}
		return true;
	}

	@Override
	protected Long @Nullable [] get(Event event) {
		List<Long> decimalPlaces = new ArrayList<>();
		Integer limit = Integer.MAX_VALUE;
		if (isLimited) {
			assert this.limit != null;
			limit = this.limit.getSingle(event);
			if (limit == null)
				return null;
		} else if (!isUnlimited) {
			limit = SkriptConfig.numberAccuracy.value();
		}
		for (Number number : numbers.getArray(event)) {
			if (!(number instanceof Double doubleValue)) {
				continue;
			}
			double x = Double.valueOf(doubleValue);
			long decimalPlace = 0L;
			for (int i = 0; i < limit; i++) {
				if (Math.abs(x % 1) > EPSILON && Math.abs((x % 1) - 1) > EPSILON) {
					x *= 10;
					decimalPlace++;
				} else {
					break;
				}
			}
			decimalPlaces.add(decimalPlace);
		}
		return decimalPlaces.toArray(Long[]::new);
	}

	@Override
	public boolean isSingle() {
		return numbers.isSingle();
	}

	@Override
	public Class<Long> getReturnType() {
		return Long.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("the number of decimal places of", numbers);
		if (isLimited) {
			assert limit != null;
			builder.append("with a limit of", limit);
		} else if (isUnlimited) {
			builder.append("with no limit");
		}
		return builder.toString();
	}

}
