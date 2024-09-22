package ch.njol.skript.classes;

import ch.njol.skript.lang.util.common.AnyProvider;

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

}
