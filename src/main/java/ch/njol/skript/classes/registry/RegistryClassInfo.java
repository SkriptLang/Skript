package ch.njol.skript.classes.registry;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.DefaultExpression;
import org.bukkit.Keyed;
import org.bukkit.Registry;

import java.util.function.Consumer;

/**
 * This class can be used for easily creating ClassInfos for {@link Registry}s.
 * It registers a language node with usage, a serializer, default expression, and a parser.
 *
 * @param <R> The Registry class.
 */
public class RegistryClassInfo<R extends Keyed> extends ClassInfo<R> {

	/**
	 * @param registryClass The registry class
	 * @param registry The registry
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 */
	public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode) {
		this(registryClass, registry, codeName, languageNode, new EventValueExpression<>(registryClass));
	}

	/**
	 * @param registryClass The registry class
	 * @param registry The registry
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param parseCallback A consumer to run on a successful parse.
	 */
	public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode, Consumer<R> parseCallback) {
		this(registryClass, registry, codeName, languageNode, new EventValueExpression<>(registryClass), parseCallback);
	}

	/**
	 * @param registryClass The registry class
	 * @param registry The registry
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param defaultExpression The default expression of the type
	 */
	public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode, DefaultExpression<R> defaultExpression) {
		this(registryClass, registry, codeName, languageNode, defaultExpression, ignored -> {});
	}

	/**
	 * @param registryClass The registry class
	 * @param registry The registry
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param defaultExpression The default expression of the type
	 * @param parseCallback A consumer to run on a successful parse.
	 */
	public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode, DefaultExpression<R> defaultExpression, Consumer<R> parseCallback) {
		super(registryClass, codeName);
		RegistryParser<R> registryParser = new RegistryParser<>(registry, languageNode, parseCallback);
		usage(registryParser.getCombinedPatterns())
			.supplier(registry::iterator)
			.serializer(new RegistrySerializer<>(registry))
			.defaultExpression(defaultExpression)
			.parser(registryParser);
	}

	/**
	 * @param registryClass The registry class
	 * @param registry The registry
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param registerComparator Whether a default comparator should be registered for this registry's classinfo
	 * @deprecated {@code registerComparator} is no longer necessary.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
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
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode, DefaultExpression<R> defaultExpression, boolean registerComparator) {
		this(registryClass, registry, codeName, languageNode, defaultExpression);
	}

}
