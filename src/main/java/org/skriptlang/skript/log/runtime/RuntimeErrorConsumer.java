package org.skriptlang.skript.log.runtime;

import java.util.logging.Level;

/**
 * Consumes runtime errors. Some use cases include printing errors to console or redirecting to a Discord channel.
 *
 * @see org.skriptlang.skript.bukkit.log.runtime.BukkitRuntimeErrorConsumer
 */
public interface RuntimeErrorConsumer {

	/**
	 * Prints a single error with all its information.
	 * @param error The error to print.
	 */
	void printError(RuntimeError error);

	/**
	 * Prints the output of a frame, including skipped errors, timeouts, and whatever other information required.
	 * @param output An output object containing unmodifiable views of the frame data.
	 * @param level The severity of this frame (error vs warning).
	 */
	void printFrameOutput(Frame.FrameOutput output, Level level);

}
