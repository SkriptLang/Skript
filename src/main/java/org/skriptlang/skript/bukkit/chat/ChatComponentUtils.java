package org.skriptlang.skript.bukkit.chat;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.util.LiteralUtils;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.ConverterInfo;

/**
 * Utilities for working with {@link Component}s.
 */
public final class ChatComponentUtils {

	private static final ConverterInfo<Object, Component> OBJECT_COMPONENT_CONVERTER =
		new ConverterInfo<>(Object.class, Component.class, ChatComponentHandler::plain, 0);

	/**
	 * Attempts to convert an expression into one that is guaranteed to return a component.
	 * @param expression The expression to convert.
	 * @return An expression that will wrap the output of {@code expression} in a {@link Component}.
	 * Will return null if {@code expression} is unable to be defended (see {@link LiteralUtils#defendExpression(Expression)}.
	 */
	public static @Nullable Expression<? extends Component> asComponentExpression(Expression<?> expression) {
		expression = LiteralUtils.defendExpression(expression);
		if (!LiteralUtils.canInitSafely(expression)) {
			return null;
		}

		//noinspection unchecked
		Expression<? extends Component> componentExpression = expression.getConvertedExpression(Component.class);
		if (componentExpression != null) {
			return componentExpression;
		}

		return new ConvertedExpression<>(expression, Component.class, OBJECT_COMPONENT_CONVERTER);
	}

	private ChatComponentUtils() { };

}
