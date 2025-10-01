package org.skriptlang.skript.log.runtime;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * Handles filtering of runtime errors based on their level and predefined limits.
 * Uses the concept of 'frames' to track the number of errors and warnings that are allowed in a given period.
 * Use {@link #test(RuntimeError)} to determine if a runtime error should be printed or not.
 * Printing the frame outputs can be done via {@link #getErrorFrame()} and {@link #getWarningFrame()}.
 */
public class RuntimeErrorFilter {

	/**
	 * A filter that does not filter anything, allowing all errors and warnings to be printed.
	 * Modifications to its limits will do nothing, and the returned frames will hold no relevant data.
	 */
	public static final RuntimeErrorFilter NO_FILTER = new RuntimeErrorFilter(
			new Frame.FrameLimit(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE),
			new Frame.FrameLimit(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE)
		) {
			@Override
			public boolean test(@NotNull RuntimeError error) {
				return true;
			}
		};

	private Frame errorFrame, warningFrame;

	public RuntimeErrorFilter(Frame.FrameLimit errorFrameLimits, Frame.FrameLimit warningFrameLimits) {
		this.errorFrame = new Frame(errorFrameLimits);
		this.warningFrame = new Frame(warningFrameLimits);
	}

	/**
	 * Tests whether a runtime error should be printed or not.
	 * @param error True if it should be printed, false if not.
	 */
	public boolean test(@NotNull RuntimeError error) {
		// print if < limit
		return (error.level() == Level.SEVERE && errorFrame.add(error))
			|| (error.level() == Level.WARNING && warningFrame.add(error));
	}

	public void setErrorFrameLimits(Frame.FrameLimit limits) {
		this.errorFrame = new Frame(limits);
	}

	public void setWarningFrameLimits(Frame.FrameLimit limits) {
		this.warningFrame = new Frame(limits);
	}

	/**
	 * @return The frame containing emitted errors.
	 */
	public Frame getErrorFrame() {
		return errorFrame;
	}

	/**
	 * @return The frame containing emitted warnings.
	 */
	public Frame getWarningFrame() {
		return warningFrame;
	}

}
