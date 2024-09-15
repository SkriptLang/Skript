/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.config;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.config.validate.EntryValidator;
import ch.njol.skript.config.validate.SectionValidator;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.NonNullPair;
import ch.njol.util.NullableChecker;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.CheckedIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * @author Peter Güttinger
 */
public class SectionNode extends Node implements Iterable<Node> {
	
	private final ArrayList<Node> nodes = new ArrayList<>();
	
	public SectionNode(final String key, final String comment, final SectionNode parent, final int lineNum) {
		super(key, comment, parent, lineNum);
	}
	
	SectionNode(final Config c) {
		super(c);
	}
	
	/**
	 * Note to self: use getNodeMap()
	 */
	@Nullable
	private NodeMap nodeMap = null;
	
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
	 * @param n
	 */
	public void add(final Node n) {
		n.remove();
		nodes.add(n);
		n.parent = this;
		n.config = config;
		getNodeMap().put(n);
	}
	
	/**
	 * Inserts the given node into this section at the specified position.
	 * 
	 * @param n
	 * @param index between 0 and {@link #size()}, inclusive
	 */
	public void insert(final Node n, final int index) {
		nodes.add(index, n);
		n.parent = this;
		n.config = config;
		getNodeMap().put(n);
	}
	
	/**
	 * Removes the given node from this section.
	 * 
	 * @param n
	 */
	public void remove(final Node n) {
		nodes.remove(n);
		n.parent = null;
		getNodeMap().remove(n);
	}
	
	/**
	 * Removes an entry with the given key.
	 * 
	 * @param key
	 * @return The removed node, or null if the key didn't match any node.
	 */
	@Nullable
	public Node remove(final String key) {
		final Node n = getNodeMap().remove(key);
		if (n == null)
			return null;
		nodes.remove(n);
		n.parent = null;
		return n;
	}
	
	/**
	 * Iterator over all non-void nodes of this section.
	 */
	@Override
	public Iterator<Node> iterator() {
		@SuppressWarnings("null")
		@NotNull
		final Iterator<Node> iter = nodes.iterator();
		return new CheckedIterator<Node>(iter, new NullableChecker<Node>() {
			@Override
			public boolean check(final @Nullable Node n) {
				return n != null && !n.isVoid();
			}
		}) {
			@Override
			public boolean hasNext() {
				final boolean hasNext = super.hasNext();
				if (!hasNext)
					SkriptLogger.setNode(SectionNode.this);
				return hasNext;
			}
			
			@Override
			@Nullable
			public Node next() {
				final Node n = super.next();
				SkriptLogger.setNode(n);
				return n;
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	/**
	 * Gets a subnode (EntryNode or SectionNode) with the specified name.
	 * 
	 * @param key
	 * @return The node with the given name
	 */
	@Nullable
	public Node get(final @Nullable String key) {
		return getNodeMap().get(key);
	}
	
	@Nullable
	public String getValue(final String key) {
		final Node n = get(key);
		if (n instanceof EntryNode)
			return ((EntryNode) n).getValue();
		return null;
	}
	
	/**
	 * Gets an entry's value or the default value if it doesn't exist or is not an EntryNode.
	 * 
	 * @param name The name of the node (case insensitive)
	 * @param def The default value
	 * @return The value of the entry node with the give node, or <tt>def</tt> if there's no entry with the given name.
	 */
	public String get(final String name, final String def) {
		final Node n = this.get(name);
		if (n == null || !(n instanceof EntryNode))
			return def;
		return ((EntryNode) n).getValue();
	}
	
	public void set(final String key, final String value) {
		final Node n = get(key);
		if (n instanceof EntryNode) {
			((EntryNode) n).setValue(value);
		} else {
			add(new EntryNode(key, value, this));
		}
	}
	
	public void set(final String key, final @Nullable Node node) {
		if (node == null) {
			remove(key);
			return;
		}
		final Node n = get(key);
		if (n != null) {
			for (int i = 0; i < nodes.size(); i++) {
				if (nodes.get(i) == n) {
					nodes.set(i, node);
					remove(n);
					getNodeMap().put(node);
					node.parent = this;
					node.config = config;
					return;
				}
			}
			assert false;
		}
		add(node);
	}
	
	void renamed(final Node node, final @Nullable String oldKey) {
		if (!nodes.contains(node))
			throw new IllegalArgumentException();
		getNodeMap().remove(oldKey);
		getNodeMap().put(node);
	}
	
	public boolean isEmpty() {
		for (final Node node : nodes) {
			if (!node.isVoid())
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
			
			if (value.isEmpty()) {
				nodes.add(new VoidNode(value, comment, this, r.getLineNum()));
				continue;
			}
			
//			if (line.startsWith("!") && line.indexOf('[') != -1 && line.endsWith("]")) {
//				final String option = line.substring(1, line.indexOf('['));
//				final String value = line.substring(line.indexOf('[') + 1, line.length() - 1);
//				if (value.isEmpty()) {
//					nodes.add(new InvalidNode(this, r));
//					Skript.error("parse options must not be empty");
//					continue;
//				} else if (option.equalsIgnoreCase("separator")) {
//					if (config.simple) {
//						Skript.warning("scripts don't have a separator");
//						continue;
//					}
//					config.separator = value;
//				} else {
//					final Node n = new InvalidNode(this, r);
//					SkriptLogger.setNode(n);
//					nodes.add(n);
//					Skript.error("unknown parse option '" + option + "'");
//					continue;
//				}
//				nodes.add(new ParseOptionNode(line.substring(0, line.indexOf('[')), this, r));
//				continue;
//			}
			
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
	public void convertToEntries(final int levels) {
		convertToEntries(levels, config.separator);
	}
	
	/**
	 * REMIND breaks saving - separator argument can be different from config.sepator
	 * 
	 * @param levels Maximum depth of recursion, <tt>-1</tt> for no limit.
	 * @param separator Some separator, e.g. ":" or "=".
	 */
	public void convertToEntries(final int levels, final String separator) {
		if (levels < -1)
			throw new IllegalArgumentException("levels must be >= -1");
		if (!config.simple)
			throw new SkriptAPIException("config is not simple: " + config);
		for (int i = 0; i < nodes.size(); i++) {
			final Node n = nodes.get(i);
			if (levels != 0 && n instanceof SectionNode) {
				((SectionNode) n).convertToEntries(levels == -1 ? -1 : levels - 1, separator);
			}
			if (!(n instanceof SimpleNode))
				continue;
			final String key = n.key;
			if (key != null)
				nodes.set(i, getEntry(key, n.comment, n.lineNum, separator));
			else
				assert false;
		}
	}
	
	@Override
	public void save(final PrintWriter w) {
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
	
	public boolean validate(final SectionValidator validator) {
		return validator.validate(this);
	}
	
	HashMap<String, String> toMap(final String prefix, final String separator) {
		final HashMap<String, String> r = new HashMap<>();
		for (final Node n : this) {
			if (n instanceof EntryNode) {
				r.put(prefix + n.getKey(), ((EntryNode) n).getValue());
			} else {
				r.putAll(((SectionNode) n).toMap(prefix + n.getKey() + separator, separator));
			}
		}
		return r;
	}

	/**
	 * Updates the values of this SectionNode based on the values of another SectionNode.
	 * @param other The other SectionNode.
	 * @param excluded Keys to exclude from this update.
	 * @return True if there are differences in the keys of this SectionNode and the other SectionNode.
	 */
	public boolean setValues(SectionNode other, String... excluded) {
		return modify(other, false, excluded);
	}

	/**
	 * Compares the keys and values of this SectionNode and another.
	 * @param other The other SectionNode.
	 * @param excluded Keys to exclude from this comparison.
	 * @return True if there are no differences in the keys and their values
	 *  of this SectionNode and the other SectionNode.
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
	
}
