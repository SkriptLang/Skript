package ch.njol.skript.lang;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class LiteralString extends VariableString implements Literal<String> {

	/**
	 * Creates a new VariableString which does not contain variables.
	 *
	 * @param input Content for string.
	 */
	protected LiteralString(String input) {
		super(input);
	}

	@Override
	public String[] getArray() {
		return new String[]{original};
	}

	@Override
	public String getSingle() {
		return original;
	}

	@Override
	public String[] getAll() {
		return new String[]{original};
	}

	@Override
	public Optional<String> getOptionalSingle(Event event) {
		return Optional.of(original);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R> @Nullable Literal<? extends R> getConvertedExpression(Class<R>... to) {
		return (Literal<? extends R>) super.getConvertedExpression(to);
	}

	/**
	 * Use {@link #toString(Event)} to get the actual string. This method is for debugging.
	 */
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return '"' + original + '"';
	}

}
