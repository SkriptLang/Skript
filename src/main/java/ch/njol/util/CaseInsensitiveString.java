package ch.njol.util;

import org.jetbrains.annotations.ApiStatus;

import java.io.Serializable;
import java.util.Locale;

import javax.annotation.Nullable;

/**
 * @deprecated use {@link java.lang.String#equalsIgnoreCase(String)}
 */
@Deprecated(forRemoval = true)
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
