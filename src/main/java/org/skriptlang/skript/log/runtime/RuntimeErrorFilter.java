package org.skriptlang.skript.log.runtime;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class RuntimeErrorFilter {

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
