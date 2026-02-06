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
	private String[] names;
	protected final Map<String, E> parseMap = new HashMap<>();
	private String[] patterns;

	/**
	 * @param enumClass The {@link Enum} {@link Class} to be accessed.
	 * @param languageNode The {@link String} representing the languageNode for the {@link Enum}
	 */
	public EnumParser(Class<E> enumClass, String languageNode) {
		assert enumClass.isEnum() : enumClass;
		assert !languageNode.isEmpty() && !languageNode.endsWith(".") : languageNode;

		this.enumClass = enumClass;
		this.languageNode = languageNode;

		refresh();
		Language.addListener(this::refresh);
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

				Noun.PluralPair singlePlural = Noun.parsePlural(first);
				String single = singlePlural.singular();
				String plural = singlePlural.plural();

				if (names[ordinal] == null) { // Add to name array if needed
					names[ordinal] = single;
				}

				parseMap.put(single, constant);
				if (!plural.isEmpty())
					parseMap.put(plural, constant);
				if (second != -1) { // There is a gender present
					parseMap.put(Noun.getArticleWithSpace(second, Language.F_INDEFINITE_ARTICLE) + single, constant);
				}
			}
		}
		patterns = parseMap.keySet().toArray(String[]::new);
	}

	@Override
	public @Nullable E parse(String string, ParseContext context) {
		return parseMap.get(string.toLowerCase(Locale.ENGLISH));
	}

	@Override
	public @Nullable E convert(String string) {
		return parse(string, ParseContext.DEFAULT);
	}

	@Override
	public String toVariableNameString(E object) {
		return toString(object, 0);
	}

	@Override
	public String[] getPatterns() {
		return patterns;
	}

	@Override
	public String toString(E object, int flags) {
		String name = names[object.ordinal()];
		return name != null ? name : object.name();
	}

}
