package org.skriptlang.skript.lang.properties;


import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;

import java.util.Locale;

public record Property<Handler extends Property.PropertyHandler<?>>(
		String name,
		SkriptAddon provider,
		@NotNull Class<? extends Handler> handler
) {
	public Property(@NotNull String name, SkriptAddon provider, @NotNull Class<? extends Handler> handler) {
		this.name = name.toLowerCase(Locale.ENGLISH);
		this.provider = provider;
		this.handler = handler;
	}

	public static <HandlerClass extends PropertyHandler<?>, Handler extends HandlerClass> Property<Handler> of(
			@NotNull String name,
			@NotNull SkriptAddon provider,
			@NotNull Class<HandlerClass> handler) {
		//noinspection unchecked
		return (Property<Handler>) new Property<>(name, provider, handler);
	}

	public static final Property<NameHandler<?, ?>> NAME = Property.of(
			"name",
			Skript.instance(),
			NameHandler.class);

	public static final Property<ContainsHandler<?, ?>> CONTAINS = Property.of(
			"contains",
			Skript.instance(),
			ContainsHandler.class);

//	public static final Property AMOUNT = new Property("amount", Skript.getAddonInstance());
//	@SuppressWarnings("unchecked")
//	public static final Property<ContainsHandler<?, ?>> CONTAINS = new Property<>("contains", Skript.getAddonInstance(), (Class<? extends ContainsHandler<?, ?>>) ContainsHandler.class);
//	public static final Property VALUED = new Property("valued", Skript.getAddonInstance());

	public static void registerDefaultProperties() {
		// Register default propertyRegistry here
		// Example: PropertyRegistry.getInstance().register(new Property("example", SkriptAddon.getInstance(), ExampleExpression.class));
		// This method can be called during addon initialization to ensure default propertyRegistry are registered.
		PropertyRegistry propertyRegistry = Skript.getPropertyRegistry();
		propertyRegistry.register(NAME);
		propertyRegistry.register(CONTAINS);
	}

	@SuppressWarnings("unused")
	public interface PropertyHandler<Type> {}

	public interface ExpressionPropertyHandler<Type, ReturnType> extends PropertyHandler<Type> {
		// Handler for the NAME property
		default Class<?> @Nullable [] acceptChange(ChangeMode mode) {
			return null;
		}
		default void change(Type named, Object @Nullable [] delta, ChangeMode mode) {
			throw new UnsupportedOperationException("Changing the name is not supported for this property.");
		}
		@NotNull Class<ReturnType> returnType();
	}

	/**
	 * no returning arrays
	 * @param <Named>
	 * @param <Name>
	 */
	public interface NameHandler<Named, Name> extends ExpressionPropertyHandler<Named, Name> {
		Name name(Named named);
	}

	public interface ContainsHandler<Container, Element> extends PropertyHandler<Container> {
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

}
