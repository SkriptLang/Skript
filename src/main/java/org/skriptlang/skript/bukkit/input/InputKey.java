package org.skriptlang.skript.bukkit.input;

import org.bukkit.Input;

import java.util.EnumSet;
import java.util.Set;

/**
 * Enum representing different movement input keys.
 * @see Input
 */
public enum InputKey {

	FORWARD,
	BACKWARD,
	RIGHT,
	LEFT,
	JUMP,
	SNEAK,
	SPRINT;

	/**
	 * Checks if the given {@link Input} is pressing this {@link InputKey}.
	 *
	 * @param input the input to check
	 * @return true if the {@link Input} is pressing this {@link InputKey}, false otherwise
	 */
	public boolean check(Input input) {
		return switch (this) {
			case FORWARD -> input.isForward();
			case BACKWARD -> input.isBackward();
			case RIGHT -> input.isRight();
			case LEFT -> input.isLeft();
			case JUMP -> input.isJump();
			case SNEAK -> input.isSneak();
			case SPRINT -> input.isSprint();
		};
	}

	/**
	 * Returns a set of {@link InputKey}s that match the given {@link Input}.
	 *
	 * @param input the input to check
	 * @return a set of {@link InputKey}s that match the given {@link Input}
	 */
	public static Set<InputKey> fromInput(Input input) {
		Set<InputKey> keys = EnumSet.noneOf(InputKey.class);
		for (InputKey key : values()) {
			if (key.check(input))
				keys.add(key);
		}
		return keys;
	}

}
