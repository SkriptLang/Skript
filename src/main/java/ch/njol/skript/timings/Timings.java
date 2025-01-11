package ch.njol.skript.timings;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Abstract base for timings in Skript
 */
public abstract class Timings {

	/**
	 * Check if this instance of timings is enabled
	 *
	 * @return Whether timings are enabled
	 */
	public abstract boolean isEnabled();

	/**
	 * Enable/disable this instance of timings
	 *
	 * @param enabled Whether timings should be enabled
	 */
	public abstract void setEnabled(boolean enabled);

	/**
	 * Reset the timings for a specific script
	 *
	 * @param script Name of script to reset timings for
	 */
	public abstract void reset(String script);

	/**
	 * Start timing something
	 * <p>
	 * If timings are not running this will return null
	 * </p>
	 *
	 * @param timeable Timeable element for timing
	 * @return Instance of the started timing
	 */
	public abstract @Nullable Timing start(Timeable timeable);

	/**
	 * Stop a timing
	 *
	 * @param timing Timing to stop
	 */
	public void stop(@Nullable Timing timing) {
		stop(timing, false);
	}

	/**
	 * Stop a timing
	 *
	 * @param timing Timing to stop
	 * @param async  Whether the element is an async operation
	 */
	public abstract void stop(@Nullable Timing timing, boolean async);

	/**
	 * Handle `/sk timings` commmand
	 *
	 * @param sender Sender of the command
	 * @param args   Sub args of the command
	 * @return Whether the command should pass
	 */
	public abstract boolean handleCommand(CommandSender sender, String[] args);

	/**
	 * Handle tab completion for the `/sk timings` command
	 *
	 * @param sender Sender of the command
	 * @param args   Sub args of the command
	 * @return List of options to show for completion
	 */
	public abstract @NotNull List<String> handleTabComplete(CommandSender sender, String[] args);

}
