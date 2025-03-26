package ch.njol.skript.util;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;

/**
 * A language utility class to be used for easily handling language values representing an Enum.
 * @param <E> Generic representing the Enum.
 * @see ch.njol.skript.classes.EnumClassInfo
 */
public final class EnumUtils<E extends Enum<E>> {

	private final Class<E> enumClass;
	private final String languageNode;

	@SuppressWarnings("NotNullFieldNotInitialized") // initialized in constructor's refresh() call
	private String[] names;
	private final HashMap<String, E> parseMap = new HashMap<>();
	
	public EnumUtils(Class<E> enumClass, String languageNode) {
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
	 * This method attempts to match the string input against one of the string representations of the enumerators.
	 * @param input a string to attempt to match against one the enumerators.
	 * @return The enumerator matching the input, or null if no match could be made.
	 */
	@Nullable
	public E parse(String input) {
		return parseMap.get(input.toLowerCase(Locale.ENGLISH));
	}

	/**
	 * This method returns the string representation of an enumerator.
	 * @param enumerator The enumerator to represent as a string.
	 * @param flags not currently used
	 * @return A string representation of the enumerator.
	 */
	public String toString(E enumerator, int flags) {
		String s = names[enumerator.ordinal()];
		return s != null ? s : enumerator.name();
	}

	/**
	 * This method returns the string representation of an enumerator
	 * @param enumerator The enumerator to represent as a string
	 * @param flag not currently used
	 * @return A string representation of the enumerator
	 */
	public String toString(E enumerator, StringMode flag) {
		return toString(enumerator, flag.ordinal());
	}

	/**
	 * @return A comma-separated string containing a list of all names representing the enumerators.
	 * Note that some entries may represent the same enumerator.
	 */
	public String getAllNames() {
		return StringUtils.join(parseMap.keySet(), ", ");
	}

}
