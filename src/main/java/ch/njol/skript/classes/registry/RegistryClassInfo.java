package ch.njol.skript.classes.registry;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.DefaultExpression;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This class can be used for easily creating ClassInfos for {@link Registry}s.
 * It registers a language node with usage, a serializer, default expression, and a parser.
 *
 * @param <R> The Registry class.
 */
public class RegistryClassInfo<R extends Keyed> extends ClassInfo<R> {

	private final @Nullable RegistryKey<R> registryKey;

	/**
	 * @param registryClass The registry class
	 * @param registryKey The registry key
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 */
	public RegistryClassInfo(Class<R> registryClass, RegistryKey<R> registryKey, String codeName, String languageNode) {
		this(registryClass, registryKey, RegistryAccess.registryAccess().getRegistry(registryKey), codeName,
			languageNode, null, null);
	}

	/**
	 * @param registryClass The registry class
	 * @param registryKey The registry key
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param parseCallback A consumer to run on a successful parse.
	 */
	public RegistryClassInfo(Class<R> registryClass, RegistryKey<R> registryKey, String codeName, String languageNode,
	                         Consumer<R> parseCallback) {
		this(registryClass, registryKey, RegistryAccess.registryAccess().getRegistry(registryKey), codeName,
			languageNode, null, parseCallback);
	}

	private RegistryClassInfo(Class<R> registryClass, @Nullable RegistryKey<R> registryKey, Registry<R> registry,
							  String codeName, String languageNode, @Nullable DefaultExpression<R> defaultExpression,
							  @Nullable Consumer<R> parseCallback) {
		super(registryClass, codeName);

		if (defaultExpression == null) {
			defaultExpression = new EventValueExpression<>(registryClass);
		}
		if (parseCallback == null) {
			parseCallback = ignored -> { };
		}

		this.registryKey = registryKey;
		RegistryParser<R> registryParser = new RegistryParser<>(registry, languageNode, parseCallback);
		usage(registryParser.getCombinedPatterns())
			.supplier(registry::iterator)
			.serializer(new RegistrySerializer<>(registry))
			.defaultExpression(defaultExpression)
			.parser(registryParser);
	}

	public @Nullable RegistryKey<R> registryKey() {
		return registryKey;
	}

	@Override
	public @NotNull RegistryParser<R> getParser() {
		//noinspection ConstantConditions, unchecked
		return (RegistryParser<R>) super.getParser();
	}

	@Override
	public @NotNull RegistrySerializer<R> getSerializer() {
		//noinspection ConstantConditions, unchecked
		return (RegistrySerializer<R>) super.getSerializer();
	}

	@Override
	public @NotNull Supplier<Iterator<R>> getSupplier() {
		//noinspection ConstantConditions
		return super.getSupplier();
	}

	/*
	 * Legacy Constructors
	 */

	/**
	 * @param registryClass The registry class
	 * @param registry The registry
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @deprecated Use {@link #RegistryClassInfo(Class, RegistryKey, String, String)}.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode) {
		this(registryClass, registry, codeName, languageNode, new EventValueExpression<>(registryClass));
	}

	/**
	 * @param registryClass The registry class
	 * @param registry The registry
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param parseCallback A consumer to run on a successful parse.
	 * @deprecated Use {@link #RegistryClassInfo(Class, RegistryKey, String, String, Consumer)}.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode, Consumer<R> parseCallback) {
		this(registryClass, registry, codeName, languageNode, new EventValueExpression<>(registryClass), parseCallback);
	}

	/**
	 * @param registryClass The registry class
	 * @param registry The registry
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param defaultExpression The default expression of the type
	 * @deprecated Use {@link #RegistryClassInfo(Class, RegistryKey, String, String)} with {@link #defaultExpression(DefaultExpression)}.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode,
							 DefaultExpression<R> defaultExpression) {
		this(registryClass, registry, codeName, languageNode, defaultExpression, ignored -> {});
	}

	/**
	 * @param registryClass The registry class
	 * @param registry The registry
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param defaultExpression The default expression of the type
	 * @param parseCallback A consumer to run on a successful parse.
	 * @deprecated Use {@link #RegistryClassInfo(Class, RegistryKey, String, String, Consumer)}
	 *  with {@link #defaultExpression(DefaultExpression)}.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode,
							 DefaultExpression<R> defaultExpression, Consumer<R> parseCallback) {
		this(registryClass, null, registry, codeName, languageNode, defaultExpression, parseCallback);
	}

	/**
	 * @param registryClass The registry class
	 * @param registry The registry
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param registerComparator Whether a default comparator should be registered for this registry's classinfo
	 * @deprecated {@code registerComparator} is no longer necessary.
	 */
	@Deprecated(since = "2.16", forRemoval = true)
	public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode, boolean registerComparator) {
		this(registryClass, registry, codeName, languageNode, new EventValueExpression<>(registryClass));
	}

	/**
	 * @param registryClass The registry class
	 * @param registry The registry
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param defaultExpression The default expression of the type
	 * @param registerComparator Whether a default comparator should be registered for this registry's classinfo
	 * @deprecated {@code registerComparator} is no longer necessary.
	 */
	@Deprecated(since = "2.16", forRemoval = true)
	public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode,
							 DefaultExpression<R> defaultExpression, boolean registerComparator) {
		this(registryClass, registry, codeName, languageNode, defaultExpression);
	}

}
