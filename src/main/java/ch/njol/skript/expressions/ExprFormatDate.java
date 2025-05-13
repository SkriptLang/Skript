package ch.njol.skript.expressions;

import java.text.SimpleDateFormat;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.VariableString;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Getter;
import ch.njol.util.Kleenean;

@Name("Formatted Date")
@Description({
	"Converts date to human-readable text format. By default, 'yyyy-MM-dd HH:mm:ss z' (e.g. '2018-03-30 16:03:12 +01') will be used. For reference, see this "
		+ "<a href=\"https://en.wikipedia.org/wiki/ISO_8601\">Wikipedia article</a>."
})
@Examples({
	"command /date:",
	"\ttrigger:",
	"\t\tsend \"Full date: %now formatted human-readable%\" to sender",
	"\t\tsend \"Short date: %now formatted as \"yyyy-MM-dd\"%\" to sender"
})
@Since("2.2-dev31, 2.7 (support variables in format)")
public class ExprFormatDate extends PropertyExpression<Date, String> {
	
	private static final SimpleDateFormat DEFAULT_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
	
	static {
		Skript.registerExpression(ExprFormatDate.class, String.class, ExpressionType.PROPERTY,
			"%dates% formatted [human-readable] [(with|as) %-string%]",
			"[human-readable] formatted %dates% [(with|as) %-string%]");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private SimpleDateFormat format;

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<String> customFormat;

	@Override
	@SuppressWarnings({"null", "unchecked"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<? extends Date>) exprs[0]);
		customFormat = (Expression<String>) exprs[1];

		boolean isSimpleString = customFormat instanceof VariableString && ((VariableString) customFormat).isSimple();
		if (customFormat instanceof Literal || isSimpleString) {
			String customFormatValue;
			if (isSimpleString) {
				customFormatValue = ((VariableString) customFormat).toString(null);
			} else {
				customFormatValue = ((Literal<String>) customFormat).getSingle();
			}

			if (customFormatValue != null) {
				try {
					format = new SimpleDateFormat(customFormatValue);
				} catch (IllegalArgumentException e) {
					Skript.error("Invalid date format: " + customFormatValue);
					return false;
				}
			}
		} else if (customFormat == null) {
			format = DEFAULT_FORMAT;
		}

		return true;
	}

	@Override
	protected String[] get(Event e, Date[] source) {
		SimpleDateFormat format;
		String formatString;

		if (customFormat != null && this.format == null) { // customFormat is not Literal or VariableString
			formatString = customFormat.getSingle(e);
			if (formatString == null)
				return null;

			try {
				format = new SimpleDateFormat(formatString);
			} catch (IllegalArgumentException ex) {
				return null;
			}
		} else {
			format = this.format;
		}

		return get(source, new Getter<String, Date>() {
			@Override
			public String get(Date date) {
				return format.format(new java.util.Date(date.getTimestamp()));
			}
		});
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return getExpr().toString(e, debug) + " formatted as " + (customFormat != null ? customFormat.toString(e, debug)
			: (format != null ? format.toPattern() : DEFAULT_FORMAT.toPattern()));
	}

}
