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

public class MultiValueExpression<T> extends WrapperExpression<T> implements DefaultExpression<T> {


	public static <T> Builder<T> builder(Class<? extends T> type) {
		return new Builder<>(type);
	}

	public static <T> MultiValueExpression<T> all(Class<? extends T> type) {
		//noinspection unchecked
		return (MultiValueExpression<T>) builder(type)
			.with(SectionValueExpression::simple)
			.with(EventValueExpression::simple)
			.build();
	}

	private final Class<? extends T> type;
	private final Set<DefaultExpression<T>> expressions;
	private final boolean single;
	private final Class<?> componentType;

	private MultiValueExpression(Class<? extends T> type, Set<DefaultExpression<T>> expressions) {
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

	public static class Builder<T> {

		private final Class<? extends T> type;
		private final Set<DefaultExpression<T>> expressions = new HashSet<>();

		private Builder(Class<? extends T> type) {
			this.type = type;
		}

		public Builder<T> with(DefaultExpression<T> expression) {
			expressions.add(expression);
			return this;
		}

		public Builder<T> with(Function<Class<? extends T>, DefaultExpression<T>> function) {
			DefaultExpression<T> expression = function.apply(type);
			expressions.add(expression);
			return this;
		}

		public MultiValueExpression<T> build() {
			Preconditions.checkArgument(expressions.size() >= 2, "Must contain atleast two expressions.");
			return new MultiValueExpression<>(type, expressions);
		}

	}

}
