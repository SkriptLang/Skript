package ch.njol.skript.update;

import java.util.function.Function;

/**
 * Allows checking whether releases are in this channel or not.
 */
public class ReleaseChannel {
	
	/**
	 * Used to check whether a release is in this channel.
	 */
	private final Function<String, Boolean> checker;
	
	/**
	 * Release channel name.
	 */
	private final String name;
	
	public ReleaseChannel(Function<String, Boolean> checker, String name) {
		this.checker = checker;
		this.name = name;
	}
	
	/**
	 * Checks whether the release with given name belongs to this channel.
	 * @param release Channel name.
	 * @return Whether the release belongs to channel or not.
	 */
	public boolean check(String release) {
		return checker.apply(release);
	}
	
	/**
	 * Gets release channel name. For example, 'beta'.
	 * @return Channel name.
	 */
	public String getName() {
		return name;
	}
}
