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

import ch.njol.skript.Skript;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.HashMap;
import java.util.Locale;

public final class EnumUtils<E extends Enum<E>> {
	
	private final Class<E> c;
	private final String languageNode;

	private String[] names;
	private final HashMap<String, E> parseMap = new HashMap<>();
	
	public EnumUtils(Class<E> c, String languageNode) {
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
	void validate(boolean force) {
		boolean update = force;

		E[] constants = c.getEnumConstants();

		if (constants.length != names.length) { // Simple check
			names = new String[constants.length];
			update = true;
		} else { // Deeper check
			for (E constant : constants) {
				if (!parseMap.containsValue(constant)) { // A new value was added to the enum
					update = true;
					break;
				}
			}
		}

		if (update) {
			parseMap.clear();
			for (E e : constants) {
				String key = languageNode + "." + e.name();
				int ordinal = e.ordinal();

				String[] values = Language.getList(key);
				for (String option : values) {
					option = option.toLowerCase(Locale.ENGLISH);
					if (values.length == 1 && option.equals(key.toLowerCase(Locale.ENGLISH))) {
						Skript.warning("Missing lang enum constant for '" + key + "'");
						continue;
					}

					NonNullPair<String, Integer> strippedOption = Noun.stripGender(option, key);
					String first = strippedOption.getFirst();
					Integer second = strippedOption.getSecond();

					if (names[ordinal] == null) { // Add to name array if needed
						names[ordinal] = first;
					}

					parseMap.put(first, e);
					if (second != -1) { // There is a gender present
						parseMap.put(Noun.getArticleWithSpace(second, Language.F_INDEFINITE_ARTICLE) + first, e);
					}
				}
			}
		}
	}
	
	@Nullable
	public E parse(String s) {
		validate(false);
		return parseMap.get(s.toLowerCase(Locale.ENGLISH));
	}

	public String toString(E e, int flags) {
		validate(false);
		return names[e.ordinal()];
	}
	
	public String getAllNames() {
		validate(false);
		return StringUtils.join(parseMap.keySet(), ", ");
	}
	
}
