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
package ch.njol.util;

import org.jetbrains.annotations.ApiStatus;

import java.io.Serializable;
import java.util.Locale;

import javax.annotation.Nullable;



/**
 * A string which is compared ignoring it's case.
 *
 * @author Peter Güttinger
 */
@Deprecated
@ApiStatus.ScheduledForRemoval
public class CaseInsensitiveString implements Serializable, Comparable<CharSequence>, CharSequence {

	private static final long serialVersionUID = 1205018864604639962L;

	private final String s;
	private final String lc;

	private final Locale locale;

	@SuppressWarnings("null")
	public CaseInsensitiveString(final String s) {
		this.s = s;
		locale = Locale.getDefault();
		lc = "" + s.toLowerCase(locale);
	}

	public CaseInsensitiveString(final String s, final Locale locale) {
		this.s = s;
		this.locale = locale;
		lc = "" + s.toLowerCase(locale);
	}

	@Override
	public int hashCode() {
		return lc.hashCode();
	}

	@SuppressWarnings("null")
	@Override
	public boolean equals(final @Nullable Object o) {
		if (o == this)
			return true;
		if (o instanceof CharSequence)
			return ((CharSequence) o).toString().toLowerCase(locale).equals(lc);
		return false;
	}

	@Override
	public String toString() {
		return s;
	}

	@Override
	public char charAt(final int i) {
		return s.charAt(i);
	}

	@Override
	public int length() {
		return s.length();
	}

	@Override
	public CaseInsensitiveString subSequence(final int start, final int end) {
		return new CaseInsensitiveString("" + s.substring(start, end), locale);
	}

	@SuppressWarnings("null")
	@Override
	public int compareTo(final CharSequence s) {
		return lc.compareTo(s.toString().toLowerCase(locale));
	}
}
