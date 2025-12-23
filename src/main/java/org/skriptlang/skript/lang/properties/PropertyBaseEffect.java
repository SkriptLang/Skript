package org.skriptlang.skript.lang.properties;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.skriptlang.skript.lang.properties.PropertyHandler.EffectHandler;

/**
 * A helper class for implementing property-driven effects.
 * @param <Handler> The handler type expected for this property.
 */
public abstract class PropertyBaseEffect<Handler extends EffectHandler<?>> extends Effect implements PropertyBaseSyntax<Handler> {

	protected static void register(Class<? extends PropertyBaseEffect<?>> effectClass, String ... patterns) {
		Skript.registerEffect(effectClass, patterns);
	}

	protected Expression<?> expr;
	protected PropertyMap<Handler> properties;
	protected final Property<Handler> property = getProperty();

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		expr = PropertyBaseSyntax.asProperty(property, exprs[0]);
		if (expr == null) {
			Skript.error(getBadTypesErrorMessage(exprs[0]));
			return false;
		}

		properties = PropertyBaseSyntax.getPossiblePropertyInfos(property, expr);
		if (properties.isEmpty()) {
			Skript.error(getBadTypesErrorMessage(expr));
			return false;
		}

		return LiteralUtils.canInitSafely(expr);
	}

	@Override
	protected void execute(Event event) {
		expr.stream(event)
			.forEach(source -> {
				Handler handler = properties.getHandler(source.getClass());
				if (handler == null)
					return;
				execute(event, handler, source);
			});
	}

	/**
	 * Calls {@link EffectHandler#execute(Object)} using {@code source}.
	 * Users that override this method may have to cast the handler to have the appropriate generics.
	 * It is guaranteed that the handler can handle the source object, but the Java generics system cannot
	 * reflect that. See the default implementation for an example of this sort of casting.
	 *
	 * @param event The current {@link Event}.
	 * @param handler The {@link EffectHandler} used for {@code source}.
	 * @param source The source object.
	 * @param <T> The type of the source object and the type the handler will accept.
	 */
	@SuppressWarnings("unchecked")
	protected <T> void execute(Event event, Handler handler, T source) {
		((EffectHandler<T>) handler).execute(source);
	}

}
