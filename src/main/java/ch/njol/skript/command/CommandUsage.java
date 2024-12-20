package ch.njol.skript.command;

import ch.njol.skript.lang.VariableString;
import ch.njol.skript.util.Utils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Holds info about the usage of a command.
 * TODO: replace with record when java 17
 */
public class CommandUsage {

	/**
	 * A dynamic usage message that can contain expressions.
	 */
	private final VariableString usage;

	/**
	 * A fallback usage message that can be used in non-event environments,
	 * like when registering the Bukkit command.
	 */
	private final String defaultUsage;

	/**
	 * @param usage The dynamic usage message, can contain expressions.
	 * @param defaultUsage A fallback usage message for use in non-event environments.
	 */
	public CommandUsage(@Nullable VariableString usage, String defaultUsage) {
		if (usage == null) {
			// Manually escape quotes. This is not a good solution, as it doesn't handle many other issues, like % in
			// commands, but in lieu of re-writing the argument parser and command logic completely, I believe this is
			// a decent stop-gap measure for using " in commands.
			defaultUsage = VariableString.quote(defaultUsage);
			usage = VariableString.newInstance(defaultUsage);
			assert usage != null;
		}
		this.usage = usage;
		this.defaultUsage = Utils.replaceChatStyles(defaultUsage);
	}

	/**
	 * @return The usage message as a {@link VariableString}.
	 */
	public VariableString getRawUsage() {
		return usage;
	}
	/**
	 * Get the usage message without an event to evaluate it.
	 * @return The evaluated usage message.
	 */
	public String getUsage() {
		return getUsage(null);
	}

	/**
	 * @param event The event used to evaluate the usage message.
	 * @return The evaluated usage message.
	 */
	public String getUsage(@Nullable Event event) {
		if (event != null || usage.isSimple())
			return usage.toString(event);
		return defaultUsage;
	}

	@Override
	public String toString() {
		return getUsage();
	}

}
