package ch.njol.skript.expressions;

import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Date;

@Name("Unix Date")
@Description("Converts given Unix timestamp to a date. The Unix timespan represents the number of seconds elapsed since 1 January 1970.")
@Examples("unix date of 946684800 #1 January 2000 12:00 AM (UTC Time)")
@Since("2.5")
public class ExprUnixDate extends SimplePropertyExpression<Number, Date> {
	
	static {
		register(ExprUnixDate.class, Date.class, "unix date", "numbers");
	}

	@Override
	@Nullable
	public Date convert(Number n) {
		return new Date((long)(n.doubleValue() * 1000));
	}

	@Override
	protected String getPropertyName() {
		return "unix date";
	}
	
	@Override
	public Class<? extends Date> getReturnType() {
		return Date.class;
	}
	
}
