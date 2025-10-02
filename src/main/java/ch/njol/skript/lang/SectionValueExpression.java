package ch.njol.skript.lang;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;

/**
 * Expression class used by {@link Section}s that use {@link SectionEvent}.
 */
public abstract class SectionValueExpression<S extends SyntaxElement & SectionValueProvider,T> extends SimpleExpression<T> implements DefaultExpression<T> {

	private final S element;
	private final Class<T> type;

	public SectionValueExpression(S element, Class<T> type) {
		this.element = element;
		this.type = type;
	}

	public S getElement() {
		return element;
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return null;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends T> getReturnType() {
		return type;
	}

}
