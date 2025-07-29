package ch.njol.skript.lang;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.SkriptParser.ExprInfo;
import ch.njol.util.StringUtils;

import java.util.List;

/**
 * Utility enum for identifying the type of error and getting the error message for {@link SkriptParser#getDefaultExpressions(ExprInfo, String)}
 */
public enum DefaultExpressionError {

	/**
	 * Error type for when a {@link DefaultExpression} can not be found for a {@link Class}.
	 */
	NOT_FOUND {
		@Override
		public String getError(List<String> codeNames, String pattern) {
			StringBuilder builder = new StringBuilder();
			String combinedComma = getCombinedComma(codeNames);
			String combinedSlash = StringUtils.join(codeNames, "/");
			builder.append(plurality(codeNames, "The class '", "The classes '"));
			builder.append(combinedComma)
				.append("'")
				.append(plurality(codeNames, " does ", " do "))
				.append("not provide a default expression. Either allow null (with %-")
				.append(combinedSlash)
				.append("%) or make it mandatory [pattern: ")
				.append(pattern)
				.append("]");
			return builder.toString();
		}
	},

	/**
	 * Error type for when the {@link DefaultExpression} for a {@link Class} is not a {@link Literal}
	 * and the pattern only accepts {@link Literal}s.
	 */
	NOT_LITERAL {
		@Override
		public String getError(List<String> codeNames, String pattern) {
			StringBuilder builder = new StringBuilder();
			builder.append(defaultExpression(codeNames, " is not a literal. ", " are not literals. "))
				.append("Either allow null (with %-*")
				.append(StringUtils.join(codeNames, "/"))
				.append("%) or make it mandatory [pattern: ")
				.append(pattern)
				.append("]");
			return builder.toString();
		}
	},

	/**
	 * Error type for when the {@link DefaultExpression} for a {@link Class} is a {@link Literal}
	 * and the pattern does not accept {@link Literal}s.
	 */
	LITERAL {
		@Override
		public String getError(List<String> codeNames, String pattern) {
			StringBuilder builder = new StringBuilder();
			builder.append(defaultExpression(codeNames, " is a literal. ", " are literals. "))
				.append("Either allow null (with %-~")
				.append(StringUtils.join(codeNames, "/"))
				.append("%) or make it mandatory [pattern: ")
				.append(pattern)
				.append("]");
			return builder.toString();
		}
	},

	/**
	 * Error type for when the {@link DefaultExpression} for a {@link Class} is plural
	 * but the pattern only accepts single.
	 */
	NOT_SINGLE {
		@Override
		public String getError(List<String> codeNames, String pattern) {
			StringBuilder builder = new StringBuilder();
			builder.append(defaultExpression(codeNames, " is not a single-element expression. ", " are not single-element expressions. "))
				.append("Change your pattern to allow multiple elements or make the expression mandatory [pattern: ")
				.append(pattern)
				.append("]");
			return builder.toString();
		}
	},

	/**
	 * Error type for when the {@link DefaultExpression} for a {@link Class} does not accept time states
	 * but the pattern infers it.
	 */
	TIME_STATE {
		@Override
		public String getError(List<String> codeNames, String pattern) {
			StringBuilder builder = new StringBuilder(defaultExpression(codeNames, " does ", " do "));
			builder.append("not have distinct time states. [pattern: ")
				.append(pattern)
				.append("]");
			return builder.toString();
		}
	};

	/**
	 * Returns an error message for the given type.
	 *
	 * @param codeNames The codeNames of {@link ClassInfo}s to include in the error message.
	 * @param pattern The pattern to include in the error message.
	 * @return error message.
	 */
	public abstract String getError(List<String> codeNames, String pattern);

	/**
	 * Utility method for constructing error messages in the format of
	 * <code>
	 *     The default expression(s) of (codenames) (single/plural)
	 *     single -> The default expression of item type is
	 *     plural -> the default expressions of item type and entity are
	 * </code>
	 *
	 * @param codeNames The list of codenames to be included in the error message.
	 * @param single The string to be formatted at the end if there is only one codename.
	 * @param plural The string to be formatted at the end if there is more than one codename.
	 * @return The formatted error message.
	 */
	private static String defaultExpression(List<String> codeNames, String single, String plural) {
		StringBuilder builder = new StringBuilder();
		String combinedComma = getCombinedComma(codeNames);
		builder.append("The default ")
			.append(plurality(codeNames, "expression ", "expressions "))
			.append("of '")
			.append(combinedComma)
			.append("'")
			.append(plurality(codeNames, single, plural));
		return builder.toString();
	}

	/**
	 * Utility method for grabbing {@code single} if {@code codeNames} is singular, otherwise {@code plural}.
	 *
	 * @param codeNames The list of codenames to be checked.
	 * @param single The string to be used if there is only one codename.
	 * @param plural The string to be used if there is more than one codename.
	 * @return {@code single} or {@code plural}.
	 */
	private static String plurality(List<String> codeNames, String single, String plural) {
		return codeNames.size() > 1 ? plural : single;
	}

	/**
	 * Utility method for combining {@code codeNames} into one string following this format.
	 * <p>
	 *     1: x
	 *     2: x and y
	 *     3 or more: x, y, and z
	 * </p>
	 * @param codeNames {@link List} of codenames to combine.
	 * @return The combined string.
	 */
	private static String getCombinedComma(List<String> codeNames) {
		assert !codeNames.isEmpty();
		if (codeNames.size() == 1) {
			return codeNames.get(0);
		} else if (codeNames.size() == 2) {
			return StringUtils.join(codeNames, " and ");
		} else {
			return StringUtils.join(codeNames, ", ", ", and ");
		}
	}

}
