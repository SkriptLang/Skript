package ch.njol.skript.config;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;

/**
 * Something that contains or references nodes and can be navigated.
 * Not all navigation options are universally-supported.
 * <br/><br/>
 * All nodes are node navigators, even if they are {@link EntryNode}s that do not support
 * children.
 * Entry nodes will return {@code null} for all child-based operations
 * (as if the requested node was simply not present).
 */
public interface NodeNavigator extends Iterable<Node> {

	/**
	 * If this navigator represents a node with no children (e.g. an entry node)
	 * this iterator will be empty.
	 *
	 * @return An iterator for all children represented by this node navigator
	 */
	default Iterator<Node> iterator() {
		return Collections.emptyIterator();
	}

	/**
	 * Obtains the immediate child node at this (direct) key.
	 * If this does not represent a node that can have children (e.g. an {@link EntryNode}) then
	 * it <em>must</em> return {@code null}.
	 *
	 * @param key The name of the node
	 * @return The child node if one is present, otherwise {@code null}
	 */
	@Nullable Node get(String key);

	/**
	 * Obtains the current node represented by this navigator.
	 * If this navigator is itself a node, it should return itself.
	 *
	 * @return The main node represented by this navigator
	 */
	@NotNull Node getCurrentNode();

	/**
	 * Fetches a node at position {@param steps} inside the current node.
	 * If any stage represents a node with no children (e.g. an entry node)
	 * the result will be null.
	 * <br/><br/>
	 * In the following example, the two entry nodes can be obtained from the root with
	 * {@code "first", "one"} and {@code "first", "two", "three"} (respectively).
	 * <pre>{@code
	 * first:
	 * 	one: value
	 * 	two:
	 * 		three: value
	 * }</pre>
	 *
	 * @param steps The node steps to traverse
	 * @return The node at the final step (or nothing)
	 */
	default @Nullable Node getNodeAt(@NotNull String @NotNull... steps) {
		Node node = this.getCurrentNode();
		for (String step : steps) {
			if (node == null)
				return null;
			node = node.get(step);
		}
		return node;
	}

	/**
	 * Fetches a node at a path inside the current node.
	 * If any stage represents a node with no children (e.g. an entry node)
	 * the result will be null.
	 * <br/><br/>
	 * In the following example, the two entry nodes can be obtained from the root with
	 * {@code "first.one"} and {@code "first.two.three"} (respectively).
	 * <pre>{@code
	 * first:
	 * 	one: value
	 * 	two:
	 * 		three: value
	 * }</pre>
	 * <br/><br/>
	 * If the path is {@code null} or empty, this node will be returned.
	 *
	 * @see	#getNodeAt(String...)
	 * @param path The path to the node, separated by {@code .}
	 * @return The node at the path (or nothing)
	 */
	@Contract("null -> this")
	default @Nullable Node getNodeAt(@Nullable String path) {
		if (path == null || path.isEmpty())
			return this.getCurrentNode();
		if (!path.contains("."))
			return this.getNodeAt(new String[]{path});
		return this.getNodeAt(path.split("\\."));
	}

	/**
	 * Gets the raw value from the node at the given path.
	 * If any part of the path is invalid, this will return null.
	 *
	 * @param path The node path from which to get the value
	 * @return If such a node exists, its value, otherwise null
	 */
	default @Nullable String getValue(String path) {
		@Nullable Node node = this.getNodeAt(path);
		if (node instanceof EntryNode entryNode)
			return entryNode.getValue();
		return null;
	}

}
