package org.skriptlang.skript.localization;

import ch.njol.skript.localization.Noun;
import ch.njol.skript.localization.Noun.PluralPair;

import static ch.njol.skript.localization.Language.F_PLURAL;

// TODO: consider reworking to unify the lang-based noun stuff too. Perhaps this becomes a parent class to the existing Noun one.

/**
 * A {@link Noun}-like class, non-dependent of {@link ch.njol.skript.localization.Language}.
 * Discerns the gender, single, and plurality of a string.
 */
public class GeneralNoun {

	private final String original;
	private int gender = 0;
	private final String singular;
	private final String plural;

	public GeneralNoun(String original) {
		this.original = original;
		String value = original;

		int genderMarker = original.indexOf("@");
		if (genderMarker != -1) {
			gender = Noun.getGender(original.substring(genderMarker + 1), original);
			value = value.substring(0, genderMarker).trim();
		}
		PluralPair pair = Noun.parsePlural(value);
		singular = pair.singular();
		plural = pair.plural();
	}

	/**
	 * Gets the gender.
	 * @return The gender.
	 * @see Noun#getGender(String, String)
	 */
	public int getGender() {
		return gender;
	}

	@Override
	public String toString() {
		return singular;
	}

	/**
	 * Gets the single or plural version of this noun.
	 * @param plural Whether to get plural version.
	 * @return The noun.
	 */
	public String toString(boolean plural) {
		return plural ? this.plural : this.singular;
	}

	/**
	 * Gets this noun appended with an article.
	 * @param flags The flags for determining plurality.
	 * @return The noun.
	 */
	public String toString(int flags) {
		return new StringBuilder()
			.append(getArticleWithSpace(flags))
			.append((flags & F_PLURAL) != 0 ? plural : singular)
			.toString();
	}

	/**
	 * Gets this noun appended with article and {@code other}.
	 * @param other The other {@link GeneralNoun} to append to the string.
	 * @param flags The flags for determining plurality.
	 * @return The noun.
	 */
	public String toString(GeneralNoun other, int flags) {
		return new StringBuilder()
			.append(getArticleWithSpace(flags))
			.append(other.toString(flags))
			.append(" ")
			.append((flags & F_PLURAL) != 0 ? plural : singular)
			.toString();
	}

	/**
	 * Returns the article appropriate for this gender & the provided flags.
	 * @param flags The flags for determining plurality.
	 * @return The article with a trailing space (as no article is possible in which case the empty string is returned)
	 * @see Noun#getArticleWithSpace(int, int)
	 */
	public String getArticleWithSpace(int flags) {
		return Noun.getArticleWithSpace(gender, flags);
	}

}
