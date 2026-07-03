package org.skriptlang.skript.bukkit.command.custom;

import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.parser.ParserInstance.Data;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Parsing data holding command context.
 */
public final class CommandParsingData extends Data {

	/**
	 * @param executableBy Describes what kind of {@link CommandSender} can execute the command.
	 * @param cooldownManager Handles cooldown management for cooldown command entries.
	 */
	public record ExecutorData(
		@Nullable ExecutableBy executableBy,
		@Nullable CooldownManager cooldownManager
	) { }

	private final LinkedList<ArgumentData<?>> arguments = new LinkedList<>();
	private final Deque<Integer> indices = new ArrayDeque<>(4);

	private final Deque<ExecutorData> executorDatas = new ArrayDeque<>(4);

	public boolean isParsingCooldownEntry = false;

	public CommandParsingData(ParserInstance parserInstance) {
		super(parserInstance);
	}

	/**
	 * @return Whether this parsing data contains any data.
	 */
	public boolean isEmpty() {
		return indices.isEmpty();
	}

	/**
	 * @return Arguments currently stored on this data, in declaration order.
	 */
	public List<ArgumentData<?>> getArguments() {
		return List.copyOf(arguments);
	}

	/**
	 * Pushes arguments to this data.
	 * @param arguments The arguments to push.
	 * @see #popArguments()
	 */
	public void pushArguments(List<ArgumentData<?>> arguments) {
		indices.push(this.arguments.size());
		this.arguments.addAll(arguments);
	}

	/**
	 * Removes the last pushed arguments from this data.
	 * @see #pushArguments(List)
	 */
	public void popArguments() {
		arguments.subList(indices.pop(), arguments.size()).clear();
	}

	/**
	 * Pushes executor data to this data.
	 * @param executorData The data to push.
	 * @see #popExecutorData()
	 */
	public void pushExecutorData(ExecutorData executorData) {
		executorDatas.push(executorData);
	}

	/**
	 * Removes the last pushed executor data from this data.
	 * @see #pushExecutorData(ExecutorData)
	 */
	public void popExecutorData() {
		executorDatas.pop();
	}

	/**
	 * Obtains the first non-null instance of a value from the pushed executors.
	 * @param function A function to obtain the desired value from the data.
	 * @return The value, or null if it was never set.
	 */
	public @Nullable <T> T getExecutorData(Function<ExecutorData, T> function) {
		for (ExecutorData executorData : executorDatas) {
			T result = function.apply(executorData);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

}
