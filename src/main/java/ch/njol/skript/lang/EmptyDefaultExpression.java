package ch.njol.skript.lang;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.DefaultValueData;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Filler {@link DefaultExpression} for registering with {@link ClassInfo}s that shouldn't have a {@link DefaultExpression}
 * but can be used with {@link DefaultValueData}
 */
public class EmptyDefaultExpression<T> extends SimpleExpression<T> implements DefaultExpression<T> {

	private final Class<T> type;

	public EmptyDefaultExpression(Class<T> type) {
		this.type = type;
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return false;
	}

	@Override
	public boolean init() {
		return false;
	}

	@Override
	protected T @Nullable [] get(Event event) {
		return null;
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
	public Class<T> getReturnType() {
		return type;
	}

	@Override
	public boolean isDefault() {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "";
	}
}
