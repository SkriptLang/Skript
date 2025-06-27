package ch.njol.skript.lang;

import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Language.LanguageListenerPriority;
import ch.njol.skript.localization.LanguageChangeListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A typed {@link SyntaxElementInfo} used to allow multi-lined patterns in the lang file and still reference the actual matched pattern
 */
public abstract class MultiPatternedSyntaxInfo<E extends SyntaxElement> extends SyntaxElementInfo<E> implements LanguageChangeListener {

	private final Class<E> dataClass;
	private final String[] codeNames;
	private final String languageNode;
	private String[] patterns;
	private final Map<String, Integer> codeNamePlacements = new HashMap<>();
	private final Map<String, String> multiPatternCorrelation = new HashMap<>();

	/**
	 * Creates a new {@link MultiPatternedSyntaxInfo} that handles {@link #onLanguageChange()} and grabbing/indexing patterns.
	 * Defaults to {@link LanguageListenerPriority#NORMAL} priority.
	 * @param dataClass The {@link SyntaxElement} class
	 * @param codeNames The codeNames to look for in the lang file
	 * @param languageNode The language node to look inside in the lang file
	 */
	public MultiPatternedSyntaxInfo(
		Class<E> dataClass,
		String[] codeNames,
		String languageNode
	) {
        this(dataClass, codeNames, languageNode,  LanguageListenerPriority.NORMAL);
	}

	/**
	 * Creates a new {@link MultiPatternedSyntaxInfo} that handles {@link #onLanguageChange()} and grabbing/indexing patterns.
	 * @param dataClass The {@link SyntaxElement} class
	 * @param codeNames The codeNames to look for in the lang file
	 * @param languageNode The language node to look inside in the lang file
	 * @param priority The priority for registering a new language listener
	 */
	public MultiPatternedSyntaxInfo(
		Class<E> dataClass,
		String[] codeNames,
		String languageNode,
		LanguageListenerPriority priority
	) {
        super(new String[codeNames.length], dataClass, dataClass.getName());
		this.dataClass = dataClass;
		this.codeNames = codeNames;
		this.languageNode = languageNode;

		for (int i = 0; i < codeNames.length; i++) {
			codeNamePlacements.put(codeNames[i], i);
		}

		Language.addListener(this, priority);
	}

	@Override
	public void onLanguageChange() {
		List<String> allPatterns = new ArrayList<>();
		for (String codeName : codeNames) {
			if (Language.keyExistsDefault(languageNode + "." + codeName + ".pattern")) {
				String pattern = patternChanger(Language.get(languageNode + "." + codeName + ".pattern"));
				multiPatternCorrelation.put(pattern, codeName);
				allPatterns.add(pattern);
			}
			if (Language.keyExistsDefault(languageNode + "." + codeName + ".patterns.0")) {
				int multiCount = 0;
				while (Language.keyExistsDefault(languageNode + "." + codeName + ".patterns." + multiCount)) {
					String pattern = patternChanger(Language.get(languageNode + "." + codeName + ".patterns." + multiCount));
					multiCount++;
					multiPatternCorrelation.put(pattern, codeName);
					allPatterns.add(pattern);
				}
			}
		}
		patterns = allPatterns.toArray(String[]::new);
	}

	/**
	 * Make any changes to the pattern when being grabbed from the lang file.
	 * @param pattern The grabbed pattern from the lang file.
	 * @return The new pattern.
	 */
	public String patternChanger(String pattern) {
		return pattern;
	}

	/**
	 * Returns the {@link SyntaxElement} class used for this {@link MultiPatternedSyntaxInfo}.
	 */
	public Class<E> getDataClass() {
		return dataClass;
	}

	/**
	 * Returns the code names to look for in the lang file.
	 */
	public String[] getCodeNames() {
		return Arrays.copyOf(codeNames, codeNames.length);
	}

	/**
	 * Returns all patterns grabbed using {@link #languageNode} and {@link #codeNames}.
	 */
	@Override
	public String[] getPatterns() {
		return Arrays.copyOf(patterns, patterns.length);
	}

	/**
	 * Returns the language node to look inside in the lang file.
	 */
	public String getLanguageNode() {
		return languageNode;
	}

	/**
	 * Uses the given {@code pattern} to grab the actual pattern index.
	 * @param pattern The pattern used.
	 * @return The actual pattern number.
	 */
	public int getPatternIndex(String pattern) {
		return codeNamePlacements.get(multiPatternCorrelation.get(pattern));
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MultiPatternedSyntaxInfo<?> other))
			return false;
        return dataClass == other.dataClass
			&& languageNode.equals(other.languageNode)
			&& Arrays.equals(codeNames, other.codeNames);
    }

}
