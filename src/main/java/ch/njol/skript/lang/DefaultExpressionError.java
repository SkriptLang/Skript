package ch.njol.skript.lang;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.SkriptParser.ExprInfo;
import ch.njol.util.StringUtils;

import java.util.List;

/**
 * Utility enum for identifying the type of error and getting the error message for {@link SkriptParser#getDefaultExpressions(ExprInfo, String)}
 */
public enum DefaultExpressionError {

	NOT_FOUND {
		@Override
		public String getError(List<String> codeNames, String pattern) {
			StringBuilder builder = new StringBuilder();
			String combinedComma = StringUtils.join(codeNames, ", ");
			String combinedSlash = StringUtils.join(codeNames, "/");
			builder.append("The class");
			if (codeNames.size() > 1)
				builder.append("es");
			builder.append(" '")
				.append(combinedComma)
				.append(" ' ");
			if (codeNames.size() > 1)  {
				builder.append("do ");
			} else {
				builder.append("does ");
			}
			builder.append("not provide a default expression. Either allow null (with %-")
				.append(combinedSlash)
				.append("%) or make it mandatory [pattern: ")
				.append(pattern)
				.append("]");
			return builder.toString();
		}
	},
	NOT_LITERAL {
		@Override
		public String getError(List<String> codeNames, String pattern) {
			StringBuilder builder = new StringBuilder();
			String combinedComma = StringUtils.join(codeNames, ", ");
			String combinedSlash = StringUtils.join(codeNames, "/");
			builder.append("The default expression");
			if (codeNames.size() > 1)
				builder.append("s");
			builder.append(" of '")
				.append(combinedComma)
				.append("' ");
			if (codeNames.size() > 1) {
				builder.append("are ");
			} else {
				builder.append("is ");
			}
			builder.append("not a literal. Either allow null (with %-*")
				.append(combinedSlash)
				.append("%) or make it mandatory [pattern: ")
				.append(pattern)
				.append("]");
			return builder.toString();
		}
	},
	LITERAL {
		@Override
		public String getError(List<String> codeNames, String pattern) {
			StringBuilder builder = new StringBuilder();
			String combinedComma = StringUtils.join(codeNames, ", ");
			String combinedSlash = StringUtils.join(codeNames, "/");
			builder.append("The default expression");
			if (codeNames.size() > 1)
				builder.append("s");
			builder.append(" of '")
				.append(combinedComma)
				.append("' ");
			if (codeNames.size() > 1) {
				builder.append("are ");
			} else {
				builder.append("is ");
			}
			builder.append("a literal. Either allow null (with %-~")
				.append(combinedSlash)
				.append("%) or make it mandatory [pattern: ")
				.append(pattern)
				.append("]");
			return builder.toString();
		}
	},
	NOT_SINGLE {
		@Override
		public String getError(List<String> codeNames, String pattern) {
			StringBuilder builder = new StringBuilder();
			String combinedComma = StringUtils.join(codeNames, ", ");
			builder.append("The default expression");
			if (codeNames.size() > 1)
				builder.append("s");
			builder.append(" of '")
				.append(combinedComma)
				.append("' ");
			if (codeNames.size() > 1) {
				builder.append("are ");
			} else {
				builder.append("is ");
			}
			builder.append("not a single-element expression. Change your pattern to allow multiple elements or make the expression mandatory [pattern: ")
				.append(pattern)
				.append("]");
			return builder.toString();
		}
	},
	TIME_STATE {
		@Override
		public String getError(List<String> codeNames, String pattern) {
			StringBuilder builder = new StringBuilder();
			String combinedComma = StringUtils.join(codeNames, ", ");
			builder.append("The default expression");
			if (codeNames.size() > 1)
				builder.append("s");
			builder.append(" of '")
				.append(combinedComma)
				.append("' ");
			if (codeNames.size() > 1) {
				builder.append("do ");
			} else {
				builder.append("does ");
			}
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

}
