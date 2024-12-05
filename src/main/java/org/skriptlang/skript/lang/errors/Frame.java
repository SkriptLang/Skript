package org.skriptlang.skript.lang.errors;

import ch.njol.skript.config.Node;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.Utils;
import org.bukkit.Bukkit;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Frame {

	final static String plural = "s were";
	final static String singular= " was";

	final String type;
	int totalLimit;
	int lineLimit;
	int lineTimeoutLimit;

	int skipped;
	int printed;
	Map<Node, Integer> lineTotals;
	Map<Node, Integer> lineSkipped;
	Map<Node, Integer> timeouts;

	public Frame(String type, int totalLimit, int lineLimit, int lineTimeoutLimit) {
		this.type = type;
		this.totalLimit = totalLimit;
		this.lineLimit = lineLimit;
		this.lineTimeoutLimit = lineTimeoutLimit;
		lineTotals = new ConcurrentHashMap<>();
		lineSkipped = new ConcurrentHashMap<>();
		timeouts = new ConcurrentHashMap<>();
	}

	boolean add(Node node) {
		// increment counter
		int lineTotal = lineTotals.compute(node, (key, count) -> (count == null ? 1 : count + 1));

		// don't print if in timeout
		if (timeouts.containsKey(node)) {
			skipped++;
			lineSkipped.compute(node, (key, count) -> (count == null ? 1 : count + 1));
			return false;
		}

		// decide whether to print
		if (printed < totalLimit && lineTotal <= lineLimit) {
			printed++;
			return true;
		} else {
			skipped++;
			lineSkipped.compute(node, (key, count) -> (count == null ? 1 : count + 1));
			if (lineTotal == lineTimeoutLimit) {
				timeouts.put(node, 10);
			}
			return false;
		}
	}

	void nextFrame() {
		skipped = 0;
		printed = 0;
		lineTotals.clear();
		lineSkipped.clear();
		for (Iterator<Map.Entry<Node, Integer>> it = timeouts.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<Node, Integer> entry = it.next();
			if (entry.getValue() > 0)
				entry.setValue(entry.getValue() - 1);
			else
				it.remove();
		}
	}

	void printFrame() {
		if (skipped > 0) {
			String linesNotDisplayed = lineSkipped.entrySet().stream()
				.map(entry -> "'"+ entry.getKey().getConfig().getFileName() + "' line " + entry.getKey().getLine() + " (" + entry.getValue() + ")")
				.collect(Collectors.joining(", "));

			SkriptLogger.sendFormatted(Bukkit.getConsoleSender(), Utils.replaceEnglishChatStyles(
				"<gold>" + (skipped) + "<light red> runtime " + type + (skipped > 1 ? plural : singular) +
					" thrown in the last second but not displayed. From: " +
					"<gray>" + linesNotDisplayed + "\n \n"));
		}

		for (Map.Entry<Node, Integer> entry : timeouts.entrySet()) {
			if (entry.getValue() == 10) {
				Node node = entry.getKey();
				SkriptLogger.sendFormatted(Bukkit.getConsoleSender(), Utils.replaceEnglishChatStyles(
					"<gold>Line " + node.getLine() + "<light red> of script '<gray>" + node.getConfig().getFileName() +
						"<light red>' produced too many runtime errors (<gray>" + lineTimeoutLimit +
						"<light red> allowed per second). No errors from this line will be printed for 10 seconds.\n \n"));
			}
		}

		// todo: send notif to online players with permission. "Script x.sk produced a runtime error! Check console."

	}
}
