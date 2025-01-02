package ch.njol.skript.log;

import java.util.logging.Filter;

import ch.njol.skript.Skript;
import ch.njol.util.LoggerFilter;

/**
 * REM: Don't even think about supporting CraftBukkit's new logging library "log4j".
 * It's probably the worst piece of shi..oftware I have ever seen used.
 * <ul>
 * <li>The interface Logger and its implementation have the same name</li>
 * <li>In general they duplicate existing code from Java (with the same names), but make it worse</li>
 * <li>You can add filters, but it's impossible to remove them</li>
 * <li>It's a miracle that it somehow even logs messages via Java's default logging system, but usually completely ignores it.</li>
 * <li>Because Level is an enum it's not possible to create your own levels, e.g. DEBUG</li>
 * </ul>
 * 
 * @author Peter Güttinger
 */
public class BukkitLoggerFilter {
	
	private static final LoggerFilter filter = new LoggerFilter(SkriptLogger.LOGGER);
	
	static {
		Skript.closeOnDisable(filter);
	}
	
	/**
	 * Adds a filter to Bukkit's log.
	 * 
	 * @param f A filter to filter log messages
	 */
	public static void addFilter(Filter f) {
		filter.addFilter(f);
	}
	
	public static boolean removeFilter(Filter f) {
		return filter.removeFilter(f);
	}
	
}
