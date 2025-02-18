package ch.njol.skript.localization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;

/**
 * @author Peter Güttinger
 */
public class RegexMessage extends Message {
	
	private final String prefix, suffix;
	
	private final int flags;
	
	@Nullable
	private Pattern pattern = null;
	
	/**
	 * A pattern that doesn't match anything
	 */
	@SuppressWarnings("null")
	public static final Pattern nop = Pattern.compile("(?!)");
	
	public RegexMessage(String key, @Nullable String prefix, @Nullable String suffix, int flags) {
		super(key);
		this.prefix = prefix == null ? "" : prefix;
		this.suffix = suffix == null ? "" : suffix;
		this.flags = flags;
	}
	
	public RegexMessage(String key, String prefix, String suffix) {
		this(key, prefix, suffix, 0);
	}
	
	public RegexMessage(String key, int flags) {
		this(key, "", "", flags);
	}
	
	public RegexMessage(String key) {
		this(key, "", "", 0);
	}
	
	@Nullable
	public Pattern getPattern() {
		validate();
		return pattern;
	}
	
	@SuppressWarnings("null")
	public Matcher matcher(String s) {
		Pattern p = getPattern();
		return p == null ? nop.matcher(s) : p.matcher(s);
	}
	
	public boolean matches(String s) {
		Pattern p = getPattern();
		return p == null ? false : p.matcher(s).matches();
	}
	
	public boolean find(String s) {
		Pattern p = getPattern();
		return p == null ? false : p.matcher(s).find();
	}
	
	@Override
	public String toString() {
		validate();
		return prefix + getValue() + suffix;
	}
	
	@Override
	protected void onValueChange() {
		try {
			pattern = Pattern.compile(prefix + getValue() + suffix, flags);
		} catch (PatternSyntaxException e) {
			Skript.error("Invalid Regex pattern '" + getValue() + "' found at '" + key + "' in the " + Language.getName() + " language file: " + e.getLocalizedMessage());
		}
	}
	
}
