package ch.njol.skript.lang;

import ch.njol.skript.expressions.ExprInput;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.util.LiteralUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Set;

/**
 * An InputSource represents a syntax that can provide a
 * value for {@link ExprInput} to use.
 * <br>
 * @see ch.njol.skript.expressions.ExprFilter
 * @see ch.njol.skript.effects.EffSort
 */
public interface InputSource {

	/**
	 * @return A mutable {@link Set} of {@link ExprInput}s that depend on this source.
	 */
	Set<ExprInput<?>> getDependentInputs();

	/**
	 * @return The current value that {@link ExprInput} should use.
	 */
	@Nullable Object getCurrentValue();

	/**
	 * {@link InputSource}s that can supply indices along with values should override this
	 * method to indicate their ability.
	 *
	 * @return Whether this source can return indices.
	 */
	default boolean hasIndices() {
		return false;
	}

	/**
	 * This should only be used by {@link InputSource}s that return true for {@link InputSource#hasIndices()}.
	 *
	 * @return The current value's index.
	 */
	default @UnknownNullability String getCurrentIndex() {
		return null;
	}

	/**
	 * Parses an expression using the given input source and parser instance.
	 *
	 * @param expr the string expression to be parsed.
	 * @param parser the parser instance used for parsing the expression.
	 * @return the parsed expression, or null if the parsing fails.
	 */
	default @Nullable Expression<?> parseExpression(String expr, ParserInstance parser, int flags) {
		InputData inputData = parser.getData(InputData.class);
		InputSource originalSource = inputData.getSource();
		inputData.setSource(this);
		Expression<?> mappingExpr = new SkriptParser(expr, flags, ParseContext.DEFAULT)
			.parseExpression(Object.class);
		if (mappingExpr != null && LiteralUtils.hasUnparsedLiteral(mappingExpr)) {
			mappingExpr = LiteralUtils.defendExpression(mappingExpr);
			if (!LiteralUtils.canInitSafely(mappingExpr))
				return null;
		}
		inputData.setSource(originalSource);
		return mappingExpr;
	}

	/**
	 * A {@link ch.njol.skript.lang.parser.ParserInstance.Data} used for
	 * linking {@link InputSource}s and {@link ExprInput}s.
	 */
	class InputData extends ParserInstance.Data {

		private @Nullable InputSource source;

		public InputData(ParserInstance parserInstance) {
			super(parserInstance);
		}

		/**
		 * {@link InputSource} should call this during init() to declare that they are the current source for future
		 * {@link ExprInput}s, and then reset it to its previous value once out of scope.
		 *
		 * @param source the source of information.
		 */
		public void setSource(@Nullable InputSource source) {
			this.source = source;
		}

		/**
		 * ExprInput should use this to get the information source, and then call
		 * {@link InputSource#getCurrentValue()} to get the current value of the source.
		 *
		 * @return the source of information.
		 */
		public @Nullable InputSource getSource() {
			return source;
		}

	}
}
