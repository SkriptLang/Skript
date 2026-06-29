package org.skriptlang.skript.bukkit.command.brigadier;

import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.parser.ParserInstance.Data;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * Parsing data holding command context.
 */
public final class CommandParsingData extends Data {

	private final LinkedList<ArgumentData<?>> arguments = new LinkedList<>();
	private final Deque<Integer> indices = new ArrayDeque<>(4);

	public CommandParsingData(ParserInstance parserInstance) {
		super(parserInstance);
	}

	/**
	 * @return Arguments currently stored on this data, in declaration order.
	 */
	public List<ArgumentData<?>> getArguments() {
		return List.copyOf(arguments);
	}

	/**
	 * Pushes arguments to this data.
	 * @param arguments The arguments to record.
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

}
