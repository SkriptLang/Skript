package ch.njol.skript.localization;

import ch.njol.util.StringUtils;

/**
 * An {@link ArgsMessage} that pluralises words following numbers. The plurals have to be in the format <tt>shel¦f¦ves¦</tt> (i.e. use 3 '¦'s).
 * 
 * @author Peter Güttinger
 */
public class PluralizingArgsMessage extends Message {
	
	public PluralizingArgsMessage(String key) {
		super(key);
	}
	
	public String toString(Object... args) {
		String val = getValue();
		if (val == null)
			return key;
		return format("" + String.format(val, args));
	}
	
	public static String format(String s) {
		StringBuilder b = new StringBuilder();
		int last = 0;
		boolean plural = false;
		for (int i = 0; i < s.length(); i++) {
			if ('0' <= s.charAt(i) && s.charAt(i) <= '9') {
				if (Math.abs(StringUtils.numberAfter(s, i)) != 1)
					plural = true;
			} else if (s.charAt(i) == '¦') {
				int c1 = s.indexOf('¦', i + 1);
				if (c1 == -1)
					break;
				int c2 = s.indexOf('¦', c1 + 1);
				if (c2 == -1)
					break;
				b.append(s.substring(last, i));
				b.append(plural ? s.substring(c1 + 1, c2) : s.substring(i + 1, c1));
				i = c2;
				last = c2 + 1;
				plural = false;
			}
		}
		if (last == 0)
			return s;
		b.append(s.substring(last, s.length()));
		return "" + b;
	}
	
}
