package org.skriptlang.skript.lang.properties;

import ch.njol.skript.classes.Changer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface PropertyHandler<Type> {
	interface ContainsHandler<Container, Element> extends PropertyHandler<Container> {
		boolean contains(Container container, Element element);

		Class<? extends Element>[] elementTypes();

		default boolean canContain(Class<?> type) {
			for (Class<? extends Element> elementType : elementTypes()) {
				if (elementType.isAssignableFrom(type)) {
					return true;
				}
			}
			return false;
		}
	}

	interface ExpressionPropertyHandler<Type, ReturnType> extends PropertyHandler<Type> {
		// Handler for the NAME property
		default Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
			return null;
		}

		default void change(Type named, Object @Nullable [] delta, Changer.ChangeMode mode) {
			throw new UnsupportedOperationException("Changing the name is not supported for this property.");
		}

		@NotNull Class<ReturnType> returnType();
	}

	/**
	 * no returning arrays
	 *
	 * @param <Named>
	 * @param <Name>
	 */
	interface NameHandler<Named, Name> extends ExpressionPropertyHandler<Named, Name> {
		Name name(Named named);
	}
}
