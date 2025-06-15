package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import org.jetbrains.annotations.Nullable;

@Name("Number of Decimal Places")
@Description("Gets the number of decimal places in a number.")
@Example("""
	set {_decimalPlaces} to the number of decimal places from 1.23456789
	# {_decimalPlaces} = 8
	""")
@Since("INSERT VERSION")
public class ExprDecimalPlaces extends SimplePropertyExpression<Number, Long> {

	static {
		register(ExprDecimalPlaces.class, Long.class, "number of decimal places", "numbers");
	}

	@Override
	public @Nullable Long convert(Number number) {
		if (!(number instanceof Double doubleValue))
			return null;
		String[] split = doubleValue.toString().split("\\.");
		if (split.length != 2)
			return null;
		return Math2.fit(0, split[1].length(), Long.MAX_VALUE);
	}

	@Override
	public Class<Long> getReturnType() {
		return Long.class;
	}

	@Override
	protected String getPropertyName() {
		return "number of decimal places";
	}

}
