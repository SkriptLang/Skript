package ch.njol.skript.classes;

import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.util.coll.iterator.ArrayIterator;

import java.util.Map;
import java.util.function.Consumer;

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
		this(enumClass, codeName, languageNode, new EventValueExpression<>(enumClass));
	}

	/**
	 * @param enumClass The class
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param parseCallback A consumer to run on a successful parse.
	 */
	public EnumClassInfo(Class<T> enumClass, String codeName, String languageNode, Consumer<T> parseCallback) {
		this(enumClass, codeName, languageNode, new EventValueExpression<>(enumClass), parseCallback);
	}

	/**
	 * @param enumClass The class
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param defaultExpression The default expression of the type
	 */
	public EnumClassInfo(Class<T> enumClass, String codeName, String languageNode, DefaultExpression<T> defaultExpression) {
		this(enumClass, codeName, languageNode, defaultExpression, ignored -> {});
	}

	/**
	 * @param enumClass The class
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param defaultExpression The default expression of the type
	 * @param parseCallback A consumer to run on a successful parse.
	 */
	public EnumClassInfo(Class<T> enumClass, String codeName, String languageNode, DefaultExpression<T> defaultExpression, Consumer<T> parseCallback) {
		super(enumClass, codeName);
		EnumParser<T> enumParser = new EnumParser<>(enumClass, languageNode, parseCallback);
		usage(enumParser.getCombinedPatterns())
			.serializer(new EnumSerializer<>(enumClass))
			.defaultExpression(defaultExpression)
			.supplier(() -> new ArrayIterator<>(enumClass.getEnumConstants()))
			.parser(enumParser);
	}

	/**
	 * @param enumClass The class
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param registerComparator Whether a default comparator should be registered for this enum's classinfo
	 * @deprecated {@code registerComparator} is no longer necessary.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public EnumClassInfo(Class<T> enumClass, String codeName, String languageNode, boolean registerComparator) {
		this(enumClass, codeName, languageNode, new EventValueExpression<>(enumClass));
	}

	/**
	 * @param enumClass The class
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param defaultExpression The default expression of the type
	 * @param registerComparator Whether a default comparator should be registered for this enum's classinfo
	 * @deprecated {@code registerComparator} is no longer necessary.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public EnumClassInfo(Class<T> enumClass, String codeName, String languageNode, DefaultExpression<T> defaultExpression, boolean registerComparator) {
		this(enumClass, codeName, languageNode, defaultExpression);
	}

}
