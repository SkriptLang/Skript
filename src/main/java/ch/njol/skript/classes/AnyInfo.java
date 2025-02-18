package ch.njol.skript.classes;

import ch.njol.skript.lang.util.common.AnyProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A special kind of {@link ClassInfo} for dealing with 'any'-accepting types.
 * These auto-generate their user patterns (e.g. {@code named} -> {@code any named thing}).
 *
 * @see AnyProvider
 */
public class AnyInfo<Type extends AnyProvider> extends ClassInfo<Type> {

	/**
	 * @param c        The class
	 * @param codeName The name used in patterns
	 */
	public AnyInfo(Class<Type> c, String codeName) {
		super(c, codeName);
		this.user("(any )?" + codeName + " (thing|object)s?");
	}

	@Override
	public ClassInfo<Type> user(String... userInputPatterns) throws PatternSyntaxException {
		if (this.userInputPatterns == null)
			return super.user(userInputPatterns);
		// Allow appending the patterns.
		List<Pattern> list = new ArrayList<>(List.of(this.userInputPatterns));
		for (String pattern : userInputPatterns) {
			list.add(Pattern.compile(pattern));
		}
		this.userInputPatterns = list.toArray(new Pattern[0]);
		return this;
	}

}
