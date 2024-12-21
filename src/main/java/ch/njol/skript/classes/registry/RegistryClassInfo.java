package ch.njol.skript.classes.registry;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.DefaultExpression;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;

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
		this(registryClass, registry, codeName, languageNode, new EventValueExpression<>(registryClass), true);
	}

	/**
	 * @param registryClass The registry class
	 * @param registry The registry
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param registerComparator Whether a default comparator should be registered for this registry's classinfo
	 */
	public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode, boolean registerComparator) {
		this(registryClass, registry, codeName, languageNode, new EventValueExpression<>(registryClass), registerComparator);
	}

	/**
	 * @param registryClass The registry class
	 * @param registry The registry
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param defaultExpression The default expression of the type
	 */
	public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode, DefaultExpression<R> defaultExpression) {
		this(registryClass, registry, codeName, languageNode,  defaultExpression, true);
	}

	/**
	 * @param registryClass The registry class
	 * @param registry The registry
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param defaultExpression The default expression of the type
	 * @param registerComparator Whether a default comparator should be registered for this registry's classinfo
	 */
	public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode, DefaultExpression<R> defaultExpression, boolean registerComparator) {
		super(registryClass, codeName);
		RegistryParser<R> registryParser = new RegistryParser<>(registry, languageNode);
		usage(registryParser.getAllNames())
			.supplier(registry::iterator)
			.serializer(new RegistrySerializer<R>(registry))
			.defaultExpression(defaultExpression)
			.parser(registryParser);

		if (registerComparator)
			Comparators.registerComparator(registryClass, registryClass, (o1, o2) -> Relation.get(o1.getKey() == o2.getKey()));
	}

}
