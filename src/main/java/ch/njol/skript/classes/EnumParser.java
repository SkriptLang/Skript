package ch.njol.skript.classes;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.util.NonNullPair;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A {@link Parser} used for parsing and handling values representing an {@link Enum}
 */
public class EnumParser<E extends Enum<E>> extends PatternedParser<E> implements Converter<String, E> {

	private final Class<E> enumClass;
	private final String languageNode;
	private final boolean isLanguage;
	private String[] names;
	private final Map<String, E> parseMap = new HashMap<>();

	/**
	 * @param enumClass The {@link Enum} {@link Class} to be accessed.
	 * @param languageNode The {@link String} representing the languageNode for the {@link Enum}
	 */
	public EnumParser(Class<E> enumClass, String languageNode) {
		this(enumClass, languageNode, true);
	}

	/**
	 * @param enumClass The {@link Enum} {@link Class} to be accessed.
	 * @param languageNode The {@link String} representing the languageNode for the {@link Enum}
	 * @param isLanguage {@link Boolean} determining if this {@link EnumParser} represents a node within 'default.lang'
	 *                   {@code True} to read the node within 'default.lang' to store the patterns and refresh from {@link Language}
	 *                   {@code False} to read the enum names and store the names
	 */
	public EnumParser(Class<E> enumClass, String languageNode, boolean isLanguage) {
		assert enumClass.isEnum() : enumClass;
		assert !languageNode.isEmpty() && !languageNode.endsWith(".") : languageNode;

		this.enumClass = enumClass;
		this.languageNode = languageNode;
		this.isLanguage = isLanguage;

		if (isLanguage) {
			refresh();
			Language.addListener(this::refresh);
		} else {
			readEnum();
		}
	}

	/**
	 * Refreshes the representation of this Enum based on the currently stored language entries.
	 */
	void refresh() {
		E[] constants = enumClass.getEnumConstants();
		names = new String[constants.length];
		parseMap.clear();
		for (E constant : constants) {
			String key = languageNode + "." + constant.name();
			int ordinal = constant.ordinal();

			String[] options = Language.getList(key);
			for (String option : options) {
				option = option.toLowerCase(Locale.ENGLISH);
				if (options.length == 1 && option.equals(key.toLowerCase(Locale.ENGLISH))) {
					String[] splitKey = key.split("\\.");
					String newKey = splitKey[1].replace('_', ' ').toLowerCase(Locale.ENGLISH) + " " + splitKey[0];
					parseMap.put(newKey, constant);
					Skript.debug("Missing lang enum constant for '" + key + "'. Using '" + newKey + "' for now.");
					continue;
				}

				// Isolate the gender if one is present
				NonNullPair<String, Integer> strippedOption = Noun.stripGender(option, key);
				String first = strippedOption.getFirst();
				Integer second = strippedOption.getSecond();

				if (names[ordinal] == null) { // Add to name array if needed
					names[ordinal] = first;
				}

				parseMap.put(first, constant);
				if (second != -1) { // There is a gender present
					parseMap.put(Noun.getArticleWithSpace(second, Language.F_INDEFINITE_ARTICLE) + first, constant);
				}
			}
		}
	}

	/**
	 * Reads the enum values of {@link #enumClass} and stores the names into {@link #parseMap}.
	 */
	private void readEnum() {
		E[] constants = enumClass.getEnumConstants();
		names = new String[constants.length];
		parseMap.clear();
		for (E constant : constants) {
			String name = constant.name().toLowerCase(Locale.ENGLISH);
			int ordinal = constant.ordinal();

			if (names[ordinal] == null)
				names[ordinal] = name;
			parseMap.put(name, constant);
			String nameWithSpaces = name.replace("_", " ");
			if (!nameWithSpaces.equals(name))
				parseMap.put(nameWithSpaces, constant);
		}
	}

	@Override
	public @Nullable E parse(String string, ParseContext context) {
		return parseMap.get(string);
	}

	@Override
	public @Nullable E convert(String string) {
		E object = parse(string, ParseContext.DEFAULT);
		if (object != null)
			return object;
		Skript.error("'" + string + "'  is not a valid option for '" + languageNode + "'. Allowed values are: " + getCombinedPatterns());
		return null;
	}

	@Override
	public String toVariableNameString(E object) {
		return toString(object, 0);
	}

	@Override
	public String[] getPatterns() {
		return parseMap.keySet().toArray(String[]::new);
	}

	@Override
	public String toString(E object, int flags) {
		String name = names[object.ordinal()];
		return name != null ? name : object.name();
	}

}
