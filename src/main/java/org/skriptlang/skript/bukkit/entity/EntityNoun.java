package org.skriptlang.skript.bukkit.entity;

import ch.njol.skript.localization.Noun;
import ch.njol.skript.localization.Noun.PluralPair;

import static ch.njol.skript.localization.Language.F_PLURAL;

/**
 * A {@link Noun}-like class, non-dependent of {@link ch.njol.skript.localization.Language}.
 * Discerns the gender, single, and plurality of a string.
 */
public class EntityNoun {

	private final String original;
	private int gender = 0;
	private String singular;
	private String plural;

	public EntityNoun(String original) {
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
		StringBuilder builder = new StringBuilder();
		builder.append(getArticleWithSpace(flags));
		builder.append((flags & F_PLURAL) != 0 ? plural : singular);
		return builder.toString();
	}

	/**
	 * Gets this noun appended with article and {@code other}.
	 * @param other The other {@link EntityNoun} to append to the string.
	 * @param flags The flags for determining plurality.
	 * @return The noun.
	 */
	public String toString(EntityNoun other, int flags) {
		StringBuilder builder = new StringBuilder();
		builder.append(getArticleWithSpace(flags));
		builder.append(other.toString(flags));
		builder.append(" ");
		builder.append((flags & F_PLURAL) != 0 ? plural : singular);
		return builder.toString();
	}

	/**
	 * Gets this noun appended with an article.
	 * @param flags The flags for determining plurality.
	 * @return The noun.
	 * @see Noun#getArticleWithSpace(int, int)
	 */
	public String getArticleWithSpace(int flags) {
		return Noun.getArticleWithSpace(gender, flags);
	}

}
