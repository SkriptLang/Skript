/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.util.Getter;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.text.DecimalFormat;

@Name("Formatted Number")
@Description(
	"Converts numbers to human-readable format. By default, '###,###' (e.g. '123,456,789') will be used. For reference, see this "
		+ "<a href=\"https://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html\" target=\"_blank\">article</a>."
)
@Examples({
	"command /formatnumber <number>:",
		"\taliases: fn",
		"\ttrigger:",
			"\t\tsend \"Formatted: %formatted arg-1%\" to sender"
})
@Since("INSERT VERSION")
public class ExprFormatNumber extends PropertyExpression<Number, String> {
	
	private static final DecimalFormat DEFAULT_FORMAT = new DecimalFormat("###,###");
	
	static {
		Skript.registerExpression(ExprFormatNumber.class, String.class, ExpressionType.PROPERTY,
				"%numbers% formatted [human-readable] [(with|as) %-string%]",
				"[human-readable] formatted %numbers% [(with|as) %-string%]");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private DecimalFormat format;

	@Nullable
	private Expression<? extends String> customFormat;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<? extends Number>) exprs[0]);
		customFormat = (Expression<? extends String>) exprs[1];

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
					format = new DecimalFormat(customFormatValue);
				} catch (IllegalArgumentException e) {
					Skript.error("Invalid number format: " + customFormatValue);
					return false;
				}
			}
		} else if (customFormat == null) {
			format = DEFAULT_FORMAT;
		}
		
		return true;
	}

	@Override
	protected String[] get(Event event, Number[] source) {
		DecimalFormat format;
		String formatString;

		if (customFormat != null && this.format == null) { // customFormat is not Literal or VariableString
			formatString = customFormat.getSingle(event);
			if (formatString == null)
				return null;

			try {
				format = new DecimalFormat(formatString);
			} catch (IllegalArgumentException e) {
				return null;
			}
		} else {
			format = this.format;
		}

		return get(source, new Getter<String, Number>() {
			@Override
			public String get(Number number) {
				return format.format(number);
			}
		});
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return getExpr().toString(event, debug) + " formatted as " + (customFormat != null ? customFormat.toString(event, debug) : DEFAULT_FORMAT.toPattern());
	}

}
