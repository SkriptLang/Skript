package org.skriptlang.skript.lang.properties;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.types.InventoryClassInfo;
import org.skriptlang.skript.bukkit.types.ItemStackClassInfo;
import org.skriptlang.skript.bukkit.types.PlayerClassInfo;
import org.skriptlang.skript.common.properties.elements.conditions.PropCondContains;
import org.skriptlang.skript.common.properties.elements.expressions.PropExprName;
import org.skriptlang.skript.common.types.QueueClassInfo;
import org.skriptlang.skript.common.types.ScriptClassInfo;
import org.skriptlang.skript.docs.Documentation;
import org.skriptlang.skript.docs.DocumentationAdapter;
import org.skriptlang.skript.docs.DocumentationDocumentable;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.lang.properties.handlers.ContainsHandler;
import org.skriptlang.skript.lang.properties.handlers.TypedValueHandler;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler;
import org.skriptlang.skript.lang.properties.handlers.base.ConditionPropertyHandler;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

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
 * See {@link PropCondContains} for an example. The implementer is responsible for using {@link PropertyBaseSyntax#asProperty(Property, Expression)}
 * and {@link PropertyBaseSyntax#getPossiblePropertyInfos(Property, Expression)} to ensure the given expression can return
 * valid types that have the given property, and then use {@link PropertyMap#get(Class)} during runtime
 * to acquire the right handler for the given type and then apply it.
 * <br>
 * <br>
 * All properties should be registered with the {@link PropertyRegistry} before use, to ensure that no conflicts between
 * registered properties occur.
 *
 * @param <Handler> the type of the handler for this property
 */
@ApiStatus.Experimental
public record Property<Handler extends PropertyHandler<?>>(
		String name,
		SkriptAddon provider,
		@NotNull Class<? extends Handler> handler,
		Documentation documentation
) implements DocumentationDocumentable {

	/**
	 * A pair of a property and a handler.
	 *
	 * @param property the property
	 * @param handler a handler for the property
	 * @param <Handler> the type of the handler
	 */
	public record PropertyInfo<Handler extends PropertyHandler<?>>(Property<Handler> property, Handler handler) { }

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
	@Contract("_, _, _ -> new")
	public static <HandlerClass extends PropertyHandler<?>, Handler extends HandlerClass> @NotNull Property<Handler> of(
		@NotNull String name,
		@NotNull SkriptAddon provider,
		@NotNull Class<HandlerClass> handler) {
		Documentation documentation = Documentation.builder()
			.name(name)
			.origin(Origin.of(provider))
			.build();
		//noinspection unchecked
		return (Property<Handler>) new Property<>(name, provider, handler, documentation);
	}

	/**
	 * Creates a new property.
	 *
	 * @param name the name of the property
	 * @param provider the addon that provides this property
	 * @param handler the handler class for this property
	 * @param documentation documentation describing this property
	 * @param <HandlerClass> the type of the handler class
	 * @param <Handler> the type of the handler
	 * @return a new property
	 */
	@Contract("_, _, _, _ -> new")
	public static <HandlerClass extends PropertyHandler<?>, Handler extends HandlerClass> @NotNull Property<Handler> of(
		@NotNull String name,
		@NotNull SkriptAddon provider,
		@NotNull Class<HandlerClass> handler,
		@NotNull Documentation documentation) {
		//noinspection unchecked
		return (Property<Handler>) new Property<>(name, provider, handler, documentation);
	}

	/**
	 * Creates a new property.
	 *
	 * @param name the name of the property
	 * @param description a brief description of the property
	 * @param provider the addon that provides this property
	 * @param handler the handler class for this property
	 * @param <HandlerClass> the type of the handler class
	 * @param <Handler> the type of the handler
	 * @return a new property
	 * @deprecated Use {@link #of(String, SkriptAddon, Class, Documentation)}.
	 */
	@Contract("_, _, _, _, _ -> new")
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public static <HandlerClass extends PropertyHandler<?>, Handler extends HandlerClass> @NotNull Property<Handler> of(
			@NotNull String name,
			@NotNull String description,
			@NotNull String since,
			@NotNull SkriptAddon provider,
			@NotNull Class<HandlerClass> handler) {
		//noinspection unchecked
		return (Property<Handler>) new Property<>(name, description, new String[]{since}, provider, handler);
	}

	/**
	 * Creates a new property.
	 *
	 * @param name the name of the property
	 * @param description a brief description of the property
	 * @param since the version[s] that this property was added/updated.
	 * @param provider the addon that provides this property
	 * @param handler the handler class for this property
	 * @param <HandlerClass> the type of the handler class
	 * @param <Handler> the type of the handler
	 * @return a new property
	 * @deprecated Use {@link #of(String, SkriptAddon, Class, Documentation)}.
	 */
	@Contract("_, _, _, _, _ -> new")
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public static <HandlerClass extends PropertyHandler<?>, Handler extends HandlerClass> @NotNull Property<Handler> of(
		@NotNull String name,
		@NotNull String description,
		@NotNull String @NotNull [] since,
		@NotNull SkriptAddon provider,
		@NotNull Class<HandlerClass> handler) {
		//noinspection unchecked
		return (Property<Handler>) new Property<>(name, description, since, provider, handler);
	}

	/* ****************************************************
	 * DEFAULT PROPERTIES
	 * ****************************************************/

	/**
	 * A property for things that have a name.
	 * @see ScriptClassInfo.ScriptNameHandler
	 */
	public static final Property<ExpressionPropertyHandler<?, ?>> NAME = Property.of(
			"name",
			Skript.instance(),
			ExpressionPropertyHandler.class,
			Documentation.of("Name",
				"A name, such as a script's name or a player's account name.",
				"2.13"));

	/**
	 * A property for things that have a display name.
	 * @see PlayerClassInfo.PlayerDisplayNameHandler
	 */
	public static final Property<ExpressionPropertyHandler<?, ?>> DISPLAY_NAME = Property.of(
			"display name",
			Skript.instance(),
			ExpressionPropertyHandler.class,
			Documentation.of("Display Name",
				"""
					A more prominently displayed name, such as a player's display name or an entity's custom name.
					Often more easily changed than the regular name.
					""",
				"2.13"));

	/**
	 * A property for checking if something contains an element.
	 * @see InventoryClassInfo.InventoryContainsHandler
	 */
	public static final Property<ContainsHandler<?, ?>> CONTAINS = Property.of(
			"contains",
			Skript.instance(),
			ContainsHandler.class,
			Documentation.of("Contains",
				"Something that can contain other things, such as an inventory or a string.",
				"2.13"));

	/**
	 * A property for getting the amount of something.
	 * @see ItemStackClassInfo.ItemStackAmountHandler
	 */
	public static final Property<ExpressionPropertyHandler<?, ?>> AMOUNT = Property.of(
			"amount",
			Skript.instance(),
			ExpressionPropertyHandler.class,
			Documentation.of("Amount",
				"The amount of something, say the number of items in a stack or in a queue.",
				"2.13"));

	/**
	 * A property for getting the size of something.
	 * @see QueueClassInfo.QueueAmountHandler
	 */
	public static final Property<ExpressionPropertyHandler<?, ?>> SIZE = Property.of(
			"size",
			Skript.instance(),
			ExpressionPropertyHandler.class,
			Documentation.of("Size",
				"The size of something, say the number of elements in a queue.",
				"2.13"));

	/**
	 * A property for getting the scale of something.
	 */
	public static final Property<ExpressionPropertyHandler<?,?>> SCALE = Property.of(
			"scale",
			Skript.instance(),
			ExpressionPropertyHandler.class,
			Documentation.of("Scale",
				"The scale of something, say the x/y/z scales of a display entity.",
				"2.14"));

	/**
	 * A property for getting the number of something.
	 */
	public static final Property<ExpressionPropertyHandler<?, ?>> NUMBER = Property.of(
			"number",
			Skript.instance(),
			ExpressionPropertyHandler.class,
			Documentation.of("Number",
				"The number of something, say the number of elements in a queue.",
				"2.13"));


	/**
	 * A property for checking whether something is empty.
	 * @see QueueClassInfo
	 */
	public static final Property<ConditionPropertyHandler<?>> IS_EMPTY = Property.of(
			"empty",
			Skript.instance(),
			ConditionPropertyHandler.class,
			Documentation.of("Is Empty",
				"Whether something is empty or not.",
				"2.13"));

	/**
	 * A property for getting a specific value of something.
	 */
	public static final Property<TypedValueHandler<?, ?>> TYPED_VALUE = Property.of(
			"typed value",
			Skript.instance(),
			TypedValueHandler.class,
			Documentation.of("Typed Value",
				"A value of a specific type, e.g. 'string value of x'.",
				"2.13"));

	/**
	 * A property for getting the x, y, or z coordinates/components of something.
	 */
	public static final Property<WXYZHandler<?, ?>> WXYZ = Property.of(
			"wxyz component",
			Skript.instance(),
			WXYZHandler.class,
			Documentation.of("W/X/Y/Z Components",
				"The W, X, Y, or Z components of something, e.g. the x coordinate of a location or vector.",
				"2.14"));

	/**
	 * A property for getting the speed of something
	 */
	public static final Property<ExpressionPropertyHandler<?,?>> SPEED = Property.of(
			"speed",
			Skript.instance(),
			ExpressionPropertyHandler.class,
			Documentation.of("Speed",
				"The speed at which something is moving.",
				"2.14"));

	/**
	 * Register all Skript's default properties. Should be done prior to loading classinfos.
	 */
	public static void registerDefaultProperties() {
		NAME.register();
		DISPLAY_NAME.register();
		CONTAINS.register();
		AMOUNT.register();
		SIZE.register();
		NUMBER.register();
		IS_EMPTY.register();
		TYPED_VALUE.register();
		SCALE.register();
		SPEED.register();
	}

	/**
	 * Creates a new property.
	 * Prefer {@link #of(String, SkriptAddon, Class, Documentation)}.
	 *
	 * @param name the name of the property
	 * @param provider the addon that provides this property
	 * @param handler the handler class for this property
	 * @param documentation documentation describing this property
	 * @see #of(String, SkriptAddon, Class, Documentation)
	 */
	public Property(String name, SkriptAddon provider, @NotNull Class<? extends Handler> handler, Documentation documentation) {
		this.name = name.toLowerCase(Locale.ENGLISH);
		this.provider = provider;
		this.handler = handler;

		// adjust documentation
		if (documentation.name().isEmpty()) {
			documentation = documentation.toBuilder()
				.name(name)
				.build();
		}
		if (documentation.origin() == Origin.UNKNOWN) {
			documentation = documentation.toBuilder()
				.origin(Origin.of(provider))
				.build();
		}
		if (documentation.id() == null) {
			documentation = documentation.toBuilder()
				.id("Prop" + documentation.autoId())
				.build();
		}
		this.documentation = documentation;
	}

	/**
	 * @deprecated Use {@link #Property(String, SkriptAddon, Class, Documentation)}.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public Property(String name, String description, String[] since, SkriptAddon provider, @NotNull Class<? extends Handler> handler) {
		this(name, provider, handler, Documentation.builder()
			.name(name)
			.description(description)
			.addSince(since)
			.origin(Origin.of(provider))
			.build());
	}

	/**
	 * Helpful registration shortcut.
	 */
	private void register() {
		this.provider.registry(PropertyRegistry.class).register(this);
	}

	@Override
	public void write(DocumentationAdapter adapter) {
		DocumentationDocumentable.super.write(adapter);

		// implementing types
		adapter.write("types", Classes.getClassInfosByProperty(this).stream()
			.map(adapter::reference)
			.toList());
	}

	/**
	 * Gets a documentation-friendly ID for this property, based on its name.
	 * May be overridden to provide a custom ID.
	 *
	 * @return a documentation-friendly ID for this property
	 * @deprecated Use {@link Documentation#id()} with {@link #documentation()}.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public String getDocumentationID() {
		String id = documentation().id();
		if (id == null) {
			id = documentation().autoId();
		}
		return id;
	}

	/**
	 * @deprecated Use {@link Documentation#description()} with {@link #documentation()}.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public String description() {
		return documentation().description();
	}

	/**
	 * @deprecated Use {@link Documentation#since()} with {@link #documentation()}.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public String[] since() {
		return documentation().since().toArray(new String[0]);
	}

}
