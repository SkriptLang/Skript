package ch.njol.skript.classes;

import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.util.coll.iterator.ArrayIterator;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;

/**
 * This class can be used for an easier writing of ClassInfos that are enums,
 * it registers a language node with usage, a serializer, default expression and a parser.
 * Making it easier to register enum ClassInfos.
 * @param <T> The enum class.
 */
public class EnumClassInfo<T extends Enum<T>> extends ClassInfo<T> {

	/**
	 * @param enumClass The class
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 */
	public EnumClassInfo(Class<T> enumClass, String codeName, String languageNode) {
		this(enumClass, codeName, languageNode, new EventValueExpression<>(enumClass), true);
	}

	/**
	 * @param enumClass The class
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param registerComparator Whether a default comparator should be registered for this enum's classinfo
	 */
	public EnumClassInfo(Class<T> enumClass, String codeName, String languageNode, boolean registerComparator) {
		this(enumClass, codeName, languageNode, new EventValueExpression<>(enumClass), registerComparator);
	}

	/**
	 * @param enumClass The class
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param defaultExpression The default expression of the type
	 */
	public EnumClassInfo(Class<T> enumClass, String codeName, String languageNode, DefaultExpression<T> defaultExpression) {
		this(enumClass, codeName, languageNode, defaultExpression, true);
	}

	/**
	 * @param enumClass The class
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param defaultExpression The default expression of the type
	 * @param registerComparator Whether a default comparator should be registered for this enum's classinfo
	 */
	public EnumClassInfo(Class<T> enumClass, String codeName, String languageNode, DefaultExpression<T> defaultExpression, boolean registerComparator) {
		super(enumClass, codeName);
		EnumParser<T> enumParser = new EnumParser<>(enumClass, languageNode);
		usage(enumParser.getCombinedPatterns())
			.serializer(new EnumSerializer<>(enumClass))
			.defaultExpression(defaultExpression)
			.supplier(() -> new ArrayIterator<>(enumClass.getEnumConstants()))
			.parser(enumParser);

		if (registerComparator)
			Comparators.registerComparator(enumClass, enumClass, (o1, o2) -> Relation.get(o1.ordinal() - o2.ordinal()));
	}

}
