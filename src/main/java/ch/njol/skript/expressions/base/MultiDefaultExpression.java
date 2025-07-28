package ch.njol.skript.expressions.base;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import com.google.common.base.Preconditions;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Typed {@link DefaultExpression} to consider usage of multiple other {@link DefaultExpression}s
 */
public class MultiDefaultExpression<T> extends WrapperExpression<T> implements DefaultExpression<T> {

	/**
	 * Creates a new {@link Builder} to build a {@link MultiDefaultExpression}.
	 *
	 * @param type The typed {@link Class}.
	 * @return {@link Builder}.
	 */
	public static <T> Builder<T> builder(Class<? extends T> type) {
		return new Builder<>(type);
	}

	/**
	 * Constructs a {@link MultiDefaultExpression} with {@link SectionValueExpression} and {@link EventValueExpression}
	 * as the {@link DefaultExpression}s to consider to be used. Check one by one respectively to see which is available.
	 * Uses the first one that successfully initializes.
	 *
	 * @param type The typed {@link Class}.
	 * @return {@link MultiDefaultExpression}.
	 */
	public static <T> MultiDefaultExpression<T> all(Class<? extends T> type) {
		//noinspection unchecked
		return (MultiDefaultExpression<T>) builder(type)
			.with(SectionValueExpression::simple)
			.with(EventValueExpression::simple)
			.build();
	}

	private final Class<? extends T> type;
	private final Set<DefaultExpression<T>> expressions;
	private final boolean single;
	private final Class<?> componentType;

	private MultiDefaultExpression(Class<? extends T> type, Set<DefaultExpression<T>> expressions) {
		this.type = type;
		this.expressions = expressions;
		single = !type.isArray();
		componentType = single ? type : type.getComponentType();
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (expressions.length != 0)
			throw new SkriptAPIException(this.getClass().getName() + " has expressions in its pattern but does not override init(...)");
		return init();
	}

	@Override
	public boolean init() {
		ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			Expression<T> matchedExpression = null;
			for (DefaultExpression<T> defaultExpression : expressions) {
				if (defaultExpression.init()) {
					matchedExpression = defaultExpression;
					break;
				}
				log.clear();
			}

			if (matchedExpression == null) {
				log.printError("There's no " + Classes.getSuperClassInfo(componentType).getName().toString(!single) + " for this event/section");
				return false;
			}

			setExpr(matchedExpression);
			log.printLog();
			return true;
		} finally {
			log.stop();
		}
	}

	@Override
	public boolean isSingle() {
		return single;
	}

	@Override
	public boolean isDefault() {
		return true;
	}

	@Override
	public Class<? extends T> getReturnType() {
		//noinspection unchecked
		return (Class<? extends T>) componentType;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return getExpr().toString(event, debug);
	}

	/**
	 * Builder class to build a {@link MultiDefaultExpression}.
	 */
	public static class Builder<T> {

		private final Class<? extends T> type;
		private final Set<DefaultExpression<T>> expressions = new HashSet<>();

		private Builder(Class<? extends T> type) {
			this.type = type;
		}

		/**
		 * Adds another {@link DefaultExpression} to consider being used.
		 * <p>
		 *     {@link MultiDefaultExpression} will check each {@link DefaultExpression} included,
		 *     in the order they're added and use the one that successfully initializes first.
		 * </p>
		 *
		 * @param expression The {@link DefaultExpression} to add.
		 * @return {@code this}.
		 */
		public Builder<T> with(DefaultExpression<T> expression) {
			expressions.add(expression);
			return this;
		}

		/**
		 * Adds another {@link DefaultExpression} to consider being used.
		 * <p>
		 *     {@link MultiDefaultExpression} will check each {@link DefaultExpression} included,
		 *     in the order they're added and use the one that successfully initializes first.
		 * </p>
		 *
		 * @param function The {@link Function} that returns a {@link DefaultExpression}.
		 * @return {@code this}.
		 */
		public Builder<T> with(Function<Class<? extends T>, DefaultExpression<T>> function) {
			DefaultExpression<T> expression = function.apply(type);
			expressions.add(expression);
			return this;
		}

		/**
		 * Finalizes this builder and builds a {@link MultiDefaultExpression}.
		 *
		 * @return {@link MultiDefaultExpression}.
		 */
		public MultiDefaultExpression<T> build() {
			Preconditions.checkArgument(expressions.size() >= 2, "Must contain atleast two expressions.");
			return new MultiDefaultExpression<>(type, expressions);
		}

	}

}
