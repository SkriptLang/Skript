/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.util;

import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.HashMap;

public final class EnumUtils<E extends Enum<E>> {
	
	private final Class<E> c;
	private final String languageNode;

	private String[] names;
	private final HashMap<String, E> parseMap = new HashMap<>();
	
	public EnumUtils(final Class<E> c, final String languageNode) {
		assert c != null && c.isEnum() : c;
		assert languageNode != null && !languageNode.isEmpty() && !languageNode.endsWith(".") : languageNode;
		
		this.c = c;
		this.languageNode = languageNode;

		names = new String[c.getEnumConstants().length];
		
		Language.addListener(() -> validate(true));
	}
	
	/**
	 * Updates the names if the language has changed or the enum was modified (using reflection).
	 */
	void validate(final boolean force) {
		boolean update = force;

		int newLength = c.getEnumConstants().length;
		if (newLength != names.length) {
			names = new String[newLength];
			update = true;
		}
		for (E constant : c.getEnumConstants()) {
			if (!parseMap.containsValue(constant)) { // A new value was added to the enum
				update = true;
				break;
			}
		}

		if (update) {
			parseMap.clear();
			for (final E e : c.getEnumConstants()) {
				String key = languageNode + "." + e.name();
				int ordinal = e.ordinal();

				for (String option : Language.getList(key)) {
					option = option.toLowerCase();
					NonNullPair<String, Integer> strippedOption = Noun.stripGender(option, key);

					if (names[ordinal] == null) { // Add to name array if needed
						names[ordinal] = strippedOption.getFirst();
					}

					parseMap.put(strippedOption.getFirst(), e);
					if (strippedOption.getSecond() != -1) { // There is a gender present
						parseMap.put(Noun.getArticleWithSpace(strippedOption.getSecond(), Language.F_INDEFINITE_ARTICLE) + strippedOption.getFirst(), e);
					}
				}
			}
		}
	}
	
	@Nullable
	public E parse(final String s) {
		validate(false);
		return parseMap.get(s.toLowerCase());
	}

	public String toString(final E e, final int flags) {
		validate(false);
		return names[e.ordinal()];
	}
	
	public String getAllNames() {
		validate(false);
		return StringUtils.join(parseMap.keySet(), ", ");
	}
	
}
