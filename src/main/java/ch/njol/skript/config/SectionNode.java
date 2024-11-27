package ch.njol.skript.config;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.config.validate.EntryValidator;
import ch.njol.skript.config.validate.SectionValidator;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.NonNullPair;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.CheckedIterator;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * Represents a node which specifies a section.
 * A section has children, which may be entries or other section nodes.
 */
public class SectionNode extends Node implements Iterable<Node> {

	private final List<Node> nodes = new ArrayList<>();

	public SectionNode(String key, String comment, SectionNode parent, int lineNum) {
		super(key, comment, parent, lineNum);
	}

	SectionNode(Config config) {
		super(config);
	}

	/**
	 * Using {@link #getNodeMap()} is recommended over this field.
	 */
	private @Nullable NodeMap nodeMap = null;

	private NodeMap getNodeMap() {
		NodeMap nodeMap = this.nodeMap;
		if (nodeMap == null) {
			nodeMap = this.nodeMap = new NodeMap();
			for (final Node node : nodes) {
				assert node != null;
				nodeMap.put(node);
			}
		}
		return nodeMap;
	}

	/**
	 * @return Total amount of nodes (including void nodes) in this section.
	 */
	public int size() {
		return nodes.size();
	}

	/**
	 * Adds the given node at the end of this section.
	 *
	 * @param node The node to add
	 */
	public void add(@NotNull Node node) {
		node.remove();
		nodes.add(node);
		node.parent = this;
		node.config = config;
		getNodeMap().put(node);
	}

	/**
	 * Inserts {@code node} into this section at the specified position.
	 *
	 * @param node  The node to insert
	 * @param index The index, between 0 and {@link #size()} (inclusive), at which to insert the node
	 */
	public void add(int index, @NotNull Node node) {
		Preconditions.checkArgument(index >= 0 && index <= size(), "index out of bounds: %s", index);

		nodes.add(index, node);
		node.parent = this;
		node.config = config;
		getNodeMap().put(node);
	}

	/**
	 * @deprecated Use {@link #add(int, Node)} instead.
	 */
	@Deprecated(forRemoval = true)
	public void insert(Node node, int index) {
		add(index, node);
	}

	/**
	 * Removes the specified node from this section.
	 *
	 * @param node The node to remove.
	 */
	public void remove(@NotNull Node node) {
		nodes.remove(node);
		node.parent = null;
		getNodeMap().remove(node);
	}

	/**
	 * Removes an entry with the given key.
	 *
	 * @param key The key of the entry to remove.
	 * @return The removed node, or null if the key didn't match any node.
	 */
	public @Nullable Node remove(@Nullable String key) {
		Node node = getNodeMap().remove(key);
		if (node == null)
			return null;
		nodes.remove(node);
		node.parent = null;
		return node;
	}

	/**
	 * Gets a subnode (EntryNode or SectionNode) with the specified name.
	 *
	 * @param key The name of the node
	 * @return The node with the given name
	 */
	public @Nullable Node get(@Nullable String key) {
		return getNodeMap().get(key);
	}

	/**
	 * Gets the node at the specified index. May be null.
	 * The index includes all nodes, including void nodes.
	 *
	 * @param idx The index of the node to get
	 * @return The node at the specified index. May be null.
	 * @throws IllegalArgumentException if the index is out of bounds
	 */
	@Nullable Node getAt(int idx) {
		Preconditions.checkArgument(idx >= 0 && idx < size(), "idx out of bounds: %s", idx);
		return nodes.get(idx);
	}

	public @Nullable String getValue(@Nullable String key) {
		Node node = get(key);
		if (node instanceof EntryNode entryNode)
			return entryNode.getValue();
		return null;
	}

	/**
	 * Gets an entry's value or the default value if it doesn't exist or is not an EntryNode.
	 *
	 * @param name The name of the node (case-insensitive)
	 * @param def  The default value
	 * @return The value of the entry node with the give node, or <tt>def</tt> if there's no entry with the given name.
	 */
	public String get(String name, String def) {
		Node node = get(name);
		if (!(node instanceof EntryNode entryNode))
			return def;
		return entryNode.getValue();
	}

	public void set(String key, String value) {
		final Node n = get(key);
		if (n instanceof EntryNode entryNode) {
			entryNode.setValue(value);
		} else {
			add(new EntryNode(key, value, this));
		}
	}

	public void set(String key, @Nullable Node node) {
		if (node == null) {
			remove(key);
			return;
		}
		Node n = get(key);
		if (n != null) {
			for (int i = 0; i < nodes.size(); i++) {
				if (nodes.get(i) != n) {
					continue;
				}
				nodes.set(i, node);
				remove(n);
				getNodeMap().put(node);
				node.parent = this;
				node.config = config;
				return;
			}
			assert false;
		}
		add(node);
	}

	void renamed(Node node, @Nullable String oldKey) {
		if (!nodes.contains(node))
			throw new IllegalArgumentException();
		getNodeMap().remove(oldKey);
		getNodeMap().put(node);
	}

	/**
	 * @return True if this section is empty, i.e. it contains only void nodes.
	 */
	public boolean isEmpty() {
		for (Node node : nodes) {
			if (!node.isVoid())
				return false;
		}
		return true;
	}

	/**
	 * @return True if this section and all children are valid, i.e. they contain no invalid nodes.
	 */
	public boolean isValid() {
		for (Node node : nodes) {
			if ((node instanceof SectionNode sectionNode && !sectionNode.isValid())
				|| node instanceof InvalidNode)
				return false;
		}
		return true;
	}

	static SectionNode load(final Config c, final ConfigReader r) throws IOException {
		return new SectionNode(c).load_i(r);
	}

	static SectionNode load(final String name, final String comment, final SectionNode parent, final ConfigReader r) throws IOException {
		parent.config.level++;
		final SectionNode node = new SectionNode(name, comment, parent, r.getLineNum()).load_i(r);
		SkriptLogger.setNode(parent);
		parent.config.level--;
		return node;
	}

	private static String readableWhitespace(final String s) {
		if (s.matches(" +"))
			return s.length() + " space" + (s.length() == 1 ? "" : "s");
		if (s.matches("\t+"))
			return s.length() + " tab" + (s.length() == 1 ? "" : "s");
		return "'" + s.replace("\t", "->").replace(' ', '_').replaceAll("\\s", "?") + "' [-> = tab, _ = space, ? = other whitespace]";
	}

	private static final Pattern fullLinePattern = Pattern.compile("([^#]|##)*#-#(\\s.*)?");

	private SectionNode load_i(final ConfigReader r) throws IOException {
		boolean indentationSet = false;
		String fullLine;
		AtomicBoolean inBlockComment = new AtomicBoolean(false);
		int blockCommentStartLine = -1;
		while ((fullLine = r.readLine()) != null) {
			SkriptLogger.setNode(this);

			if (!inBlockComment.get()) // this will be updated for the last time at the start of the comment
				blockCommentStartLine = this.getLine();
			final NonNullPair<String, String> line = Node.splitLine(fullLine, inBlockComment);
			String value = line.getFirst();
			final String comment = line.getSecond();

			final SectionNode parent = this.parent;
			if (!indentationSet && parent != null && parent.parent == null && !value.isEmpty() && !value.matches("\\s*") && !value.matches("\\S.*")) {
				final String s = value.replaceFirst("\\S.*$", "");
				assert !s.isEmpty() : fullLine;
				if (s.matches(" +") || s.matches("\t+")) {
					config.setIndentation(s);
					indentationSet = true;
				} else {
					nodes.add(new InvalidNode(value, comment, this, r.getLineNum()));
					Skript.error("indentation error: indent must only consist of either spaces or tabs, but not mixed (found " + readableWhitespace(s) + ")");
					continue;
				}
			}
			if (!value.matches("\\s*") && !value.matches("^(" + config.getIndentation() + "){" + config.level + "}\\S.*")) {
				if (value.matches("^(" + config.getIndentation() + "){" + config.level + "}\\s.*") || !value.matches("^(" + config.getIndentation() + ")*\\S.*")) {
					nodes.add(new InvalidNode(value, comment, this, r.getLineNum()));
					final String s = "" + value.replaceFirst("\\S.*$", "");
					Skript.error("indentation error: expected " + config.level * config.getIndentation().length() + " " + config.getIndentationName() + (config.level * config.getIndentation().length() == 1 ? "" : "s") + ", but found " + readableWhitespace(s));
					continue;
				} else {
					if (parent != null && !config.allowEmptySections && isEmpty()) {
						Skript.warning("Empty configuration section! You might want to indent one or more of the subsequent lines to make them belong to this section" +
							" or remove the colon at the end of the line if you don't want this line to start a section.");
					}
					r.reset();
					return this;
				}
			}

			value = value.trim();

			if (value.isEmpty()) { // entire line is a comment or empty
				nodes.add(new VoidNode(value, comment, this, r.getLineNum()));
				continue;
			}

			if (value.endsWith(":") && (config.simple
				|| value.indexOf(config.separator) == -1
				|| config.separator.endsWith(":") && value.indexOf(config.separator) == value.length() - config.separator.length()
			)) {
				boolean matches = false;
				try {
					matches = fullLine.contains("#") && fullLinePattern.matcher(fullLine).matches();
				} catch (StackOverflowError e) { // Probably a very long line
					Node.handleNodeStackOverflow(e, fullLine);
				}
				if (!matches) {
					nodes.add(SectionNode.load("" + value.substring(0, value.length() - 1), comment, this, r));
					continue;
				}
			}

			if (config.simple) {
				nodes.add(new SimpleNode(value, comment, r.getLineNum(), this));
			} else {
				nodes.add(getEntry(value, comment, r.getLineNum(), config.separator));
			}

		}
		if (inBlockComment.get()) {
			Skript.error("A block comment (###) was opened on line " + blockCommentStartLine + " but never closed.");
		}
		SkriptLogger.setNode(parent);

		return this;
	}

	private Node getEntry(final String keyAndValue, final String comment, final int lineNum, final String separator) {
		final int x = keyAndValue.indexOf(separator);
		if (x == -1) {
			final InvalidNode in = new InvalidNode(keyAndValue, comment, this, lineNum);
			EntryValidator.notAnEntryError(in, separator);
			SkriptLogger.setNode(this);
			return in;
		}
		final String key = "" + keyAndValue.substring(0, x).trim();
		final String value = "" + keyAndValue.substring(x + separator.length()).trim();
		return new EntryNode(key, value, comment, this, lineNum);
	}

	/**
	 * Converts all SimpleNodes in this section to EntryNodes.
	 *
	 * @param levels Amount of levels to go down, e.g. 0 to only convert direct subnodes of this section or -1 for all subnodes including subnodes of subnodes etc.
	 */
	public void convertToEntries(int levels) {
		convertToEntries(levels, config.separator);
	}

	/**
	 * REMIND breaks saving - separator argument can be different from config.sepator
	 *
	 * @param levels    Maximum depth of recursion, <tt>-1</tt> for no limit.
	 * @param separator Some separator, e.g. ":" or "=".
	 */
	public void convertToEntries(int levels, String separator) {
		if (levels < -1)
			throw new IllegalArgumentException("levels must be >= -1");
		if (!config.simple)
			throw new SkriptAPIException("config is not simple: " + config);
		for (int i = 0; i < nodes.size(); i++) {
			Node n = nodes.get(i);
			if (levels != 0 && n instanceof SectionNode sectionNode)
				sectionNode.convertToEntries(levels == -1 ? -1 : levels - 1, separator);
			if (!(n instanceof SimpleNode))
				continue;
			String key = n.key;
			if (key != null) {
				nodes.set(i, getEntry(key, n.comment, n.lineNum, separator));
			} else {
				assert false;
			}
		}
	}

	@Override
	public void save(PrintWriter w) {
		if (parent != null)
			super.save(w);
		for (final Node node : nodes)
			node.save(w);
	}

	@Override
	String save_i() {
		assert key != null;
		return key + ":";
	}

	public boolean validate(SectionValidator validator) {
		return validator.validate(this);
	}

	Map<String, String> toMap(String prefix, String separator) {
		Map<String, String> result = new HashMap<>();
		for (Node node : this) {
			if (node instanceof EntryNode entryNode) {
				result.put(prefix + node.getKey(), entryNode.getValue());
			} else {
				result.putAll(((SectionNode) node).toMap(prefix + node.getKey() + separator, separator));
			}
		}
		return result;
	}

	/**
	 * Updates the values of this SectionNode based on the values of another SectionNode.
	 *
	 * @param other    The other SectionNode.
	 * @param excluded Keys to exclude from this update.
	 * @return True if there are differences in the keys of this SectionNode and the other SectionNode.
	 */
	public boolean setValues(SectionNode other, String... excluded) {
		return modify(other, false, excluded);
	}

	/**
	 * Compares the keys and values of this SectionNode and another.
	 *
	 * @param other    The other SectionNode.
	 * @param excluded Keys to exclude from this comparison.
	 * @return True if there are no differences in the keys and their values
	 * of this SectionNode and the other SectionNode.
	 */
	public boolean compareValues(SectionNode other, String... excluded) {
		return !modify(other, true, excluded); // invert as "modify" returns true if different
	}

	private boolean modify(SectionNode other, boolean compareValues, String... excluded) {
		boolean different = false;

		for (Node node : this) {
			if (CollectionUtils.containsIgnoreCase(excluded, node.key))
				continue;

			Node otherNode = other.get(node.key);
			if (otherNode != null) { // other has this key
				if (node instanceof SectionNode) {
					if (otherNode instanceof SectionNode) {
						different |= ((SectionNode) node).modify((SectionNode) otherNode, compareValues);
					} else { // Our node type is different from the old one
						different = true;
						if (compareValues) // Counting values means we don't need to copy over values
							break;
					}
				} else if (node instanceof EntryNode) {
					if (otherNode instanceof EntryNode) {
						String ourValue = ((EntryNode) node).getValue();
						String theirValue = ((EntryNode) otherNode).getValue();
						if (compareValues) {
							if (!ourValue.equals(theirValue)) {
								different = true;
								break; // Counting values means we don't need to copy over values
							}
						} else { // If we don't care about values, just copy over the old one
							((EntryNode) node).setValue(theirValue);
						}
					} else { // Our node type is different from the old one
						different = true;
						if (compareValues) // Counting values means we don't need to copy over values
							break;
					}
				}
			} else { // other is missing this key (which means we have a new key)
				different = true;
				if (compareValues) // Counting values means we don't need to copy over values
					break;
			}
		}

		if (!different) {
			for (Node otherNode : other) {
				if (this.get(otherNode.key) == null) {
					different = true;
					break;
				}
			}
		}

		return different;
	}

	/**
	 * @return An iterator over all non-void nodes in this section.
	 */
	@Override
	public @NotNull Iterator<Node> iterator() {
		return new CheckedIterator<>(fullIterator(),
			n -> Objects.nonNull(n) && !n.isVoid()); // double null check to avoid warning
	}

	/**
	 * @return An iterator over all nodes in this section, including void nodes.
	 */
	@NotNull Iterator<Node> fullIterator() {
		return new CheckedIterator<>(nodes.iterator(), Objects::nonNull) {
			@Override
			public boolean hasNext() {
				boolean hasNext = super.hasNext();
				if (!hasNext)
					SkriptLogger.setNode(SectionNode.this);
				return hasNext;
			}

			@Override
			public @Nullable Node next() {
				Node node = super.next();
				SkriptLogger.setNode(node);
				return node;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

}
