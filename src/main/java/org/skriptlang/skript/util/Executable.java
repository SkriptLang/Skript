package org.skriptlang.skript.util;

/**
 * A process that can be executed, which may return its result.
 * This is an abstraction for general triggers (runnables, tasks, functions, etc.)
 * without any Bukkit dependency.
 *
 * @param <Caller> The source type of this process (e.g. Event, CommandSender, etc.)
 * @param <Result> The return type of this process.
 */
public interface Executable<Caller, Result> {

	Result execute(Caller caller, Object... arguments);

}
