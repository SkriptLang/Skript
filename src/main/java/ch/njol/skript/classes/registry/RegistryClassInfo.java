package ch.njol.skript.classes.registry;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.DefaultExpression;
import org.bukkit.Keyed;
import org.bukkit.Registry;

/**
 * This class can be used for easily creating ClassInfos for {@link Registry}s.
 * It registers a language node with usage, a serializer, default expression, and a parser.
 *
 * @param <R> The Registry class.
 */
public class RegistryClassInfo<R extends Keyed> extends ClassInfo<R> {

	public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode) {
		this(registryClass, registry, codeName, languageNode, new EventValueExpression<>(registryClass));
	}

	/**
	 * @param registry The registry
	 * @param codeName The name used in patterns
	 */
	public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode, DefaultExpression<R> defaultExpression) {
		super(registryClass, codeName);
		RegistryParser<R> registryParser = new RegistryParser<>(registry, languageNode);
		usage(registryParser.getAllNames())
			.supplier(registry::iterator)
			.serializer(new RegistrySerializer<R>(registry))
			.defaultExpression(defaultExpression)
			.parser(registryParser);
	}

}
