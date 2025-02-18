package org.skriptlang.skript.bukkit.input.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInputEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.input.InputKey;

import java.util.Set;

public class EvtPlayerInput extends SkriptEvent {

	static {
		if (Skript.classExists("org.bukkit.event.player.PlayerInputEvent")) {
			Skript.registerEvent("Player Input", EvtPlayerInput.class, PlayerInputEvent.class,
					"[player] (toggle|toggling|1:press[ing]|2:release|2:releasing) of (%-inputkeys%|(an|any) input key)",
					"([player] %-inputkeys%|[an|any [player]] input key) (toggle|toggling|1:press[ing]|2:release|2:releasing)")
				.description("Called when a player sends an updated input to the server.",
					"Note: The input keys event value is the set of keys the player is currently pressing, not the keys that were pressed or released.")
				.examples("on any input key press:",
					"\tsend \"You are pressing: %event-inputkeys%\" to player")
				.since("2.10")
				.requiredPlugins("Minecraft 1.21.3+");
		}
	}

	private @Nullable Literal<InputKey> keysToCheck;
	private InputType type;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		//noinspection unchecked
		keysToCheck = (Literal<InputKey>) args[0];
		type = InputType.values()[parseResult.mark];
		return true;
	}

	@Override
	public boolean check(Event event) {
		PlayerInputEvent inputEvent = (PlayerInputEvent) event;
		Set<InputKey> previousKeys = InputKey.fromInput(inputEvent.getPlayer().getCurrentInput());
		Set<InputKey> currentKeys = InputKey.fromInput(inputEvent.getInput());
		Set<InputKey> keysToCheck = this.keysToCheck != null ? Set.of(this.keysToCheck.getAll()) : null;
		boolean and = this.keysToCheck != null && this.keysToCheck.getAnd();
		return type.checkInputKeys(previousKeys, currentKeys, keysToCheck, and);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("player");
		builder.append(type.name().toLowerCase());
		builder.append(keysToCheck == null ? "any input key" : keysToCheck);
		return builder.toString();
	}

	private enum InputType {

		TOGGLE {
			@Override
			public boolean checkKeyState(boolean inPrevious, boolean inCurrent) {
				return inPrevious != inCurrent;
			}
		},
		PRESS {
			@Override
			public boolean checkKeyState(boolean inPrevious, boolean inCurrent) {
				return !inPrevious && inCurrent;
			}
		},
		RELEASE {
			@Override
			public boolean checkKeyState(boolean inPrevious, boolean inCurrent) {
				return inPrevious && !inCurrent;
			}
		};

		/**
		 * Checks the state of a key based on its presence in the previous and current sets of keys.
		 *
		 * @param inPrevious true if the key was present in the previous set of keys, false otherwise
		 * @param inCurrent true if the key is present in the current set of keys, false otherwise
		 * @return true if the key state matches the condition defined by the input type, false otherwise
		 */
		public abstract boolean checkKeyState(boolean inPrevious, boolean inCurrent);

		/**
		 * Checks the input keys based on the previous and current sets of keys.
		 * <br>
		 * {@code previous} and {@code current} are never the same.
		 *
		 * @param previous the set of keys before the input change
		 * @param current the set of keys after the input change
		 * @param keysToCheck the set of keys to check against, can be null
		 * @param and true if the keys to check must all be present, false if any key is enough
		 * @return true if the condition is met based on the input type, false otherwise
		 */
		public boolean checkInputKeys(Set<InputKey> previous, Set<InputKey> current, @Nullable Set<InputKey> keysToCheck, boolean and) {
			if (keysToCheck == null) {
				return switch (this) {
					case TOGGLE -> true;
					case PRESS -> previous.size() <= current.size();
					case RELEASE -> previous.size() >= current.size();
				};
			}
			for (InputKey key : keysToCheck) {
				boolean inPrevious = previous.contains(key);
				boolean inCurrent = current.contains(key);
				if (and && !checkKeyState(inPrevious, inCurrent)) {
					return false;
				} else if (!and && checkKeyState(inPrevious, inCurrent)) {
					return true;
				}
			}
			return and;
		}

	}

}
