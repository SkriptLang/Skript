package org.skriptlang.skript.util;

import org.jetbrains.annotations.ApiStatus;

/**
 * Something that can become invalid due to external conditions, such as:
 * <ul>
 *     <li>A newer copy of the same thing becomes available.</li>
 *     <li>It is a finalised view that no longer reflects the real thing.</li>
 *     <li>Some backing object has been deleted or irreparably modified.</li>
 * </ul>
 * <br/>
 * Once this has been {@link #invalidate()}d, its {@link #valid()} method should
 * return {@code false}.
 * <br/>
 * When this is no longer {@link #valid()}, it may no longer be safe or accurate to use.
 * <br/>
 * Implementations may expose their own (re)validate method, but this is outside
 * the scope of this interface and so is not provided by default.
 */
public interface Validated {

	/**
	 * Mark a thing as no longer safe to use. This should (typically) not be used by
	 * external modifiers, but implementations may differ.
	 * <br/>
	 * Implementations that do <em>not</em> want to expose an invalidator hook
	 * may throw an {@link UnsupportedOperationException}, which is protected by
	 * the internal contract.
	 *
	 * @throws UnsupportedOperationException If this is not something that can be
	 *                                       externally invalidated.
	 */
	@ApiStatus.Internal
	void invalidate() throws UnsupportedOperationException;

	/**
	 * Implementations ought to specify what 'valid' means locally (e.g. should a new copy be obtained?)
	 * when overriding this method.
	 *
	 * @return Whether this is still valid
	 */
	boolean valid();

	/**
	 * @return A single-use validator marker with thread safety
	 */
	static Validated validator() {
		return new Validator();
	}

}

class Validator implements Validated {

	private volatile boolean valid = true;

	@Override
	public synchronized void invalidate() {
		this.valid = false;
	}

	@Override
	public boolean valid() {
		return valid;
	}

}
