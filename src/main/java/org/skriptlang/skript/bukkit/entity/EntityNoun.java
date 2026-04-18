package org.skriptlang.skript.bukkit.entity;

import ch.njol.skript.localization.Noun;
import ch.njol.skript.localization.Noun.PluralPair;

import static ch.njol.skript.localization.Language.F_PLURAL;

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

	public int getGender() {
		return gender;
	}

	@Override
	public String toString() {
		return singular;
	}

	public String toString(boolean plural) {
		return plural ? this.plural : this.singular;
	}

	public String toString(int flags) {
		StringBuilder builder = new StringBuilder();
		builder.append(getArticleWithSpace(flags));
		builder.append((flags & F_PLURAL) != 0 ? plural : singular);
		return builder.toString();
	}

	public String toString(EntityNoun other, int flags) {
		StringBuilder builder = new StringBuilder();
		builder.append(getArticleWithSpace(flags));
		builder.append(other.toString(flags));
		builder.append(" ");
		builder.append((flags & F_PLURAL) != 0 ? plural : singular);
		return builder.toString();
	}

	public String getArticleWithSpace(int flags) {
		return Noun.getArticleWithSpace(gender, flags);
	}

}
