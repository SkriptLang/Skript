package org.skriptlang.skript.lang.properties;


import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.base.types.InventoryClassInfo;
import org.skriptlang.skript.bukkit.base.types.PlayerClassInfo;
import org.skriptlang.skript.common.conditions.PropCondContains;
import org.skriptlang.skript.common.expressions.PropExprName;
import org.skriptlang.skript.common.types.ScriptClassInfo;
import org.skriptlang.skript.lang.properties.PropertyHandler.ContainsHandler;
import org.skriptlang.skript.lang.properties.PropertyHandler.ExpressionPropertyHandler;

import java.util.Locale;

/**
 * A property that can be applied to certain types of objects.
 * A property has a name, a provider (the addon that provides it), and a handler class.
 * The handler class is responsible for defining the concrete behavior of the property.
 * A pre-existing handler class may be used, such as {@link ExpressionPropertyHandler} or a new one may be created by
 * implementing {@link PropertyHandler}. All implementations of this property on {@link ClassInfo}s will then adhere to
 * the behavior defined in the handler class.
 * <br>
 * For example, the {@link #NAME} property uses the {@link ExpressionPropertyHandler}, which provides a standard set
 * of behaviors for properties that can be expressed as an expression, such as getting the value of the property and
 * changing it via {@link ExpressionPropertyHandler#change(Object, Object[], Changer.ChangeMode)}.
 * <br>
 * The {@link #CONTAINS} property uses the {@link ContainsHandler}, which is an example of a custom handler that
 * provides more specialized behavior.
 * <br>
 * <br>
 * Properties can be used in syntaxes in two ways: <br>
 * 1) Using {@link PropertyBaseExpression}. This expression handles all the complexities of properties for any property
 * handler that implements {@link ExpressionPropertyHandler}. See {@link PropExprName} for an example.<br>
 * 2) By implementing the property directly in a custom syntax. This is more complex, but allows for more flexibility.
 * See {@link PropCondContains} for an example. The implementer is responsible for using {@link PropertyUtils#asProperty(Property, Expression)}
 * and {@link PropertyUtils#getPossiblePropertyInfos(Property, Expression)} to ensure the given expression can return
 * valid types that have the given property, and then use {@link PropertyUtils.PropertyMap#get(Class)} during runtime
 * to acquire the right handler for the given type and then apply it.
 * <br>
 * <br>
 * All properties should be registered with the {@link PropertyRegistry} before use, to ensure that no conflicts between
 * registered properties occur.
 *
 * @param <Handler> the type of the handler for this property
 */
public record Property<Handler extends PropertyHandler<?>>(
		String name,
		SkriptAddon provider,
		@NotNull Class<? extends Handler> handler
) {
	/**
	 * Creates a new property. Prefer {@link #of(String, SkriptAddon, Class)}.
	 *
	 * @param name the name of the property
	 * @param provider the addon that provides this property
	 * @param handler the handler class for this property
	 * @see #of(String, SkriptAddon, Class)
	 */
	public Property(@NotNull String name, SkriptAddon provider, @NotNull Class<? extends Handler> handler) {
		this.name = name.toLowerCase(Locale.ENGLISH);
		this.provider = provider;
		this.handler = handler;
	}

	/**
	 * Creates a new property.
	 *
	 * @param name the name of the property
	 * @param provider the addon that provides this property
	 * @param handler the handler class for this property
	 * @param <HandlerClass> the type of the handler class
	 * @param <Handler> the type of the handler
	 * @return a new property
	 */
	public static <HandlerClass extends PropertyHandler<?>, Handler extends HandlerClass> Property<Handler> of(
			@NotNull String name,
			@NotNull SkriptAddon provider,
			@NotNull Class<HandlerClass> handler) {
		//noinspection unchecked
		return (Property<Handler>) new Property<>(name, provider, handler);
	}

	/**
	 * A property for things that have a name.
	 * @see ScriptClassInfo.ScriptNameHandler
	 */
	public static final Property<ExpressionPropertyHandler<?, ?>> NAME = Property.of(
			"name",
			Skript.instance(),
			ExpressionPropertyHandler.class);

	/**
	 * A property for things that have a display name.
	 * @see PlayerClassInfo.PlayerDisplayNameHandler
	 */
	public static final Property<ExpressionPropertyHandler<?, ?>> DISPLAY_NAME = Property.of(
			"display name",
			Skript.instance(),
			ExpressionPropertyHandler.class);

	/**
	 * A property for checking if something contains an element.
	 * @see InventoryClassInfo.InventoryContainsHandler
	 */
	public static final Property<ContainsHandler<?, ?>> CONTAINS = Property.of(
			"contains",
			Skript.instance(),
			ContainsHandler.class);

	// TODO: other common properties
	//	public static final Property AMOUNT = new Property("amount", Skript.getAddonInstance());
	//	public static final Property VALUED = new Property("valued", Skript.getAddonInstance());

	public static void registerDefaultProperties() {
		PropertyRegistry propertyRegistry = Skript.getAddonInstance().registry(PropertyRegistry.class);
		propertyRegistry.register(NAME);
		propertyRegistry.register(CONTAINS);
	}

	/**
	 * A pair of a property and a handler.
	 *
	 * @param property the property
	 * @param handler a handler for the property
	 * @param <Handler> the type of the handler
	 */
	public record PropertyInfo<Handler extends PropertyHandler<?>>(Property<Handler> property, Handler handler) { }
}
