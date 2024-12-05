package org.skriptlang.skript.lang.errors;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.log.SkriptLogger;
import org.bukkit.Bukkit;

/**
 * Handles duplicate and spammed runtime errors.
 */
public class RuntimeErrorManager {

	private final Frame errorFrame, warningFrame;

	public RuntimeErrorManager() {
		errorFrame = new Frame("error", 8, 2, 4);
		warningFrame = new Frame("warning", 8, 2, 4);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Skript.getInstance(), () -> {
			errorFrame.printFrame();
			errorFrame.nextFrame();

			warningFrame.printFrame();
			warningFrame.nextFrame();
		}, 20, 20);
	}

	void error(Node node, String message) {
		// print if < limit
		if (errorFrame.add(node)) {
			SkriptLogger.sendFormatted(Bukkit.getConsoleSender(), message);
		}
	}

	void warning(Node node, String message){
		// print if < limit
		if (warningFrame.add(node)) {
			SkriptLogger.sendFormatted(Bukkit.getConsoleSender(), message);
		}
	}

	public Frame getErrorFrame() {
		return errorFrame;
	}

	public Frame getWarningFrame() {
		return warningFrame;
	}
}
