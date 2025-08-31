package org.skriptlang.skript.lang.properties;


import ch.njol.skript.Skript;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.lang.properties.PropertyHandler.ContainsHandler;
import org.skriptlang.skript.lang.properties.PropertyHandler.NameHandler;

import java.util.Locale;

public record Property<Handler extends PropertyHandler<?>>(
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
		PropertyRegistry propertyRegistry = Skript.getAddonInstance().registry(PropertyRegistry.class);
		propertyRegistry.register(NAME);
		propertyRegistry.register(CONTAINS);
	}

	public record PropertyInfo<Handler extends PropertyHandler<?>>(Property<Handler> property, Handler handler) {
	}
}
