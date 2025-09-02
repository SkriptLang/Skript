package ch.njol.skript.config;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.common.AnyNamed;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.util.Validated;

import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The parent class of all nodes.
 */
public abstract class Node implements AnyNamed, Validated, NodeNavigator {

	@Nullable
	protected String key;

	protected String comment = "";

	/**
	 * An array of all comments that are associated with this node.
	 */
	protected String[] comments = new String[0];

	@Unmodifiable
	public @NotNull Collection<String> comments() {
		return List.of(comments);
	}

	protected final int lineNum;

	private final boolean debug;

	@Nullable
	protected SectionNode parent;
	protected Config config;

//	protected Node() {
//		key = null;
//		debug = false;
//		lineNum = -1;
//		SkriptLogger.setNode(this);
//	}

	protected Node(final Config c) {
		key = null;
		debug = false;
		lineNum = -1;
		config = c;
		SkriptLogger.setNode(this);
	}

	protected Node(final String key, final SectionNode parent) {
		this.key = key;
		debug = false;
		lineNum = -1;
		this.parent = parent;
		config = parent.getConfig();
		SkriptLogger.setNode(this);
	}

	protected Node(final String key, final String comment, final SectionNode parent, final int lineNum) {
		this.key = key;
		this.comment = comment;
		debug = comment.equals("#DEBUG#");
		this.lineNum = lineNum;
		this.parent = parent;
		config = parent.getConfig();
		SkriptLogger.setNode(this);
	}

	Node(@Nullable String key, @NotNull String comment, @NotNull String @NotNull [] comments, SectionNode parent, int lineNum) {
		this.key = key;
		this.debug = comment.equals("#DEBUG#");
		this.comment = comment;
		this.comments = comments;
		this.lineNum = lineNum;
		this.parent = parent;
		this.config = parent.getConfig();

		SkriptLogger.setNode(this);
	}

//	protected Node(final String key, final SectionNode parent, final ConfigReader r) {
//		this(key, parent, r.getLine(), r.getLineNum());
//	}
//
	/**
	 * Key of this node. <tt>null</tt> for empty or invalid nodes, and the config's main node.
	 */
	@Nullable
	public String getKey() {
		return key;
	}

	public final Config getConfig() {
		return config;
	}

	public void rename(final String newname) {
		if (key == null)
			throw new IllegalStateException("can't rename an anonymous node");
		final String oldKey = key;
		key = newname;
		if (parent != null)
			parent.renamed(this, oldKey);
	}

	public void move(final SectionNode newParent) {
		final SectionNode p = parent;
		if (p == null)
			throw new IllegalStateException("can't move the main node");
		p.remove(this);
		newParent.add(this);
	}

	/**
	 * Splits a line into value and comment.
	 * <p>
	 * Whitespace is preserved (whitespace in front of the comment is added to the value), and any ## in the value are replaced by a single #. The comment is returned with a
	 * leading #, except if there is no comment in which case it will be the empty string.
	 *
	 * @param line the line to split
	 * @return A pair (value, comment).
	 */
	public static NonNullPair<String, String> splitLine(String line) {
		return splitLine(line, new AtomicBoolean(false));
	}

	/**
	 * Splits a line into value and comment.
	 * <p>
	 * Whitespace is preserved (whitespace in front of the comment is added to the value), and any ## not in quoted strings in the value are replaced by a single #. The comment is returned with a
	 * leading #, except if there is no comment in which case it will be the empty string.
	 *
	 * @param line the line to split
	 * @param inBlockComment Whether we are currently inside a block comment
	 * @return A pair (value, comment).
	 */
	public static NonNullPair<String, String> splitLine(String line, AtomicBoolean inBlockComment) {
		String trimmed = line.trim();
		if (trimmed.equals("###")) { // we start or terminate a BLOCK comment
			inBlockComment.set(!inBlockComment.get());
			return new NonNullPair<>("", line);
		} else if (trimmed.startsWith("#")) {
			return new NonNullPair<>("", line.substring(line.indexOf('#')));
		} else if (inBlockComment.get()) { // we're inside a comment, all text is a comment
			return new NonNullPair<>("", line);
		}

		// idea: find first # that is not within a string or variable name. Use state machine to determine whether a # is a comment or not.
		int length = line.length();
		StringBuilder finalLine = new StringBuilder(line);
		int numRemoved = 0;
		SplitLineState state = SplitLineState.CODE;
		SplitLineState previousState = SplitLineState.CODE; // stores the state prior to entering %, so it can be re-set when leaving
		// find next " or %
		for (int i = 0; i < length; i++) {
			char c = line.charAt(i);
			// check for things that can be escaped by doubling
			if (c == '%' || c == '"' || c == '#') {
				// skip if doubled (only skip ## outside of strings)
				if ((c != '#' || state != SplitLineState.STRING) && i + 1 < length && line.charAt(i + 1) == c) {
					if (c == '#') { // remove duplicate #
						finalLine.deleteCharAt(i - numRemoved);
						numRemoved++;
					}
					i++;
					continue;
				}
				SplitLineState tmp = state;
				state = SplitLineState.update(c, state, previousState);
				if (state == SplitLineState.HALT)
					return new NonNullPair<>(finalLine.substring(0, i - numRemoved), line.substring(i));
				// only update previous state when we go from !CODE -> CODE due to %
				if (c == '%' && state == SplitLineState.CODE)
					previousState = tmp;
			}
		}
		return new NonNullPair<>(finalLine.toString(), "");
	}

	/**
	 * state machine:<br>
	 * ": CODE -> STRING,  			STRING -> CODE, 	VARIABLE -> VARIABLE<br>
	 * %: CODE -> PREVIOUS_STATE, 	STRING -> CODE, 	VARIABLE -> CODE<br>
	 * {: CODE -> VARIABLE, 		STRING -> STRING,	VARIABLE -> VARIABLE<br>
	 * }: CODE -> CODE, 			STRING -> STRING, 	VARIABLE -> CODE<br>
	 * #: CODE -> HALT, 			STRING -> STRING, 	VARIABLE -> HALT<br>
	 * invalid characters simply return given state.<br>
	 */
	private enum SplitLineState {
		HALT,
		CODE,
		STRING,
		VARIABLE;

		/**
		 * Updates the state given a character input.
		 * @param c character input. '"', '%', '{', '}', and '#' are valid.
		 * @param state the current state of the machine
		 * @param previousState the state of the machine when it last entered a % CODE % section
		 * @return the new state of the machine
		 */
		private static SplitLineState update(char c, SplitLineState state, SplitLineState previousState) {
			if (state == HALT)
				return HALT;

			switch (c) {
				case '%':
					if (state == CODE)
						return previousState;
					return CODE;
				case '"':
					switch (state) {
						case CODE:
							return STRING;
						case STRING:
							return CODE;
						default:
							return state;
					}
				case '{':
					if (state == STRING)
						return STRING;
					return VARIABLE;
				case '}':
					if (state == STRING)
						return STRING;
					return CODE;
				case '#':
					if (state == STRING)
						return STRING;
					return HALT;
			}
			return state;
		}
	}

	static void handleNodeStackOverflow(StackOverflowError e, String line) {
		Node n = SkriptLogger.getNode();
		SkriptLogger.setNode(null); // Avoid duplicating the which node error occurred in parentheses on every error message

		Skript.error("There was a StackOverFlowError occurred when loading a node. This maybe from your scripts, aliases or Skript configuration.");
		Skript.error("Please make your script lines shorter! Do NOT report this to SkriptLang unless it occurs with a short script line or built-in aliases!");

		Skript.error("");
		Skript.error("Updating your Java and/or using respective 64-bit versions for your operating system may also help and is always a good practice.");
		Skript.error("If it is still not fixed, try moderately increasing the thread stack size (-Xss flag) in your startup script.");
		Skript.error("");
		Skript.error("Using a different Java Virtual Machine (JVM) like OpenJ9 or GraalVM may also help; though be aware that not all plugins may support them.");
		Skript.error("");

		Skript.error("Line that caused the issue:");

		// Print the line caused the issue for diagnosing (will be very long most probably), in case of someone pasting this in an issue and not providing the code.
		Skript.error(line);

		// If testing (assertions enabled) - print the whole stack trace.
		if (Skript.testing()) {
			Skript.exception(e);
		}

		SkriptLogger.setNode(n); // Revert the node back
	}

	@Nullable
	protected String getComment() {
		return comment;
	}

	int getLevel() {
		int l = 0;
		Node n = this;
		while ((n = n.parent) != null) {
			l++;
		}
		return Math.max(0, l - 1);
	}

	protected String getIndentation() {
		return StringUtils.multiply(config.getIndentation(), getLevel());
	}

	/**
	 * @return String to save this node as. The correct indentation and the comment will be added automatically, as well as all '#'s will be escaped.
	 */
	abstract String save_i();

	/**
	 * @return This node and its comments as a collection of lines.
	 */
	public @Unmodifiable @NotNull Collection<String> getAsStrings() {
		String[] strings = new String[comments.length + 1];

		for (int i = 0; i < comments.length; i++) {
			if (comments[i].isEmpty()) { // dont indent for empty lines
				strings[i] = "";
			} else {
				strings[i] = getIndentation() + comments[i];
			}
		}
		strings[comments.length] = getIndentation()
			+ escapeUnquotedHashtags(save_i())
			+ (comment.isEmpty() ? "" : " " + comment);

		return List.of(strings);
	}

	public final String save() {
		return getIndentation() + escapeUnquotedHashtags(save_i()) + comment;
	}

	/**
	 * @deprecated Use {@link #getAsStrings()} instead.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public void save(final PrintWriter w) {
		w.println(save());
	}

	private static String escapeUnquotedHashtags(String input) {
		int length = input.length();
		StringBuilder output = new StringBuilder(input);
		int numAdded = 0;
		SplitLineState state = SplitLineState.CODE;
		SplitLineState previousState = SplitLineState.CODE;
		// find next " or %
		for (int i = 0; i < length; i++) {
			char c = input.charAt(i);
			// check for things that can be escaped by doubling
			if (c == '%' || c == '"' || c == '#') {
				// escaped #s outside of strings
				if (c == '#' && state != SplitLineState.STRING) {
					output.insert(i + numAdded, "#");
					numAdded++;
					continue;
				}
				// skip if doubled (not #s)
				if (i + 1 < length && input.charAt(i + 1) == c) {
					i++;
					continue;
				}
				SplitLineState tmp = state;
				state = SplitLineState.update(c, state, previousState);
				previousState = tmp;
			}
		}
		return output.toString();
	}


	@Nullable
	public SectionNode getParent() {
		return parent;
	}

	/**
	 * Removes this node from its parent. Does nothing if this node does not have a parent node.
	 */
	public void remove() {
		final SectionNode p = parent;
		if (p == null)
			return;
		p.remove(this);
	}

	/**
	 * @return Original line of this node at the time it was loaded. <tt>-1</tt> if this node was created dynamically.
	 */
	public int getLine() {
		return lineNum;
	}

	/**
	 * @return Whether this node does not hold information (i.e. is empty or invalid)
	 */
	public boolean isVoid() {
		return this instanceof VoidNode;// || this instanceof ParseOptionNode;
	}

	/**
	 * returns information about this node which looks like the following:<br/>
	 * <code>node value #including comments (config.sk, line xyz)</code>
	 */
	@Override
	public String toString() {
		if (parent == null)
			return config.getFileName();
		return save_i()
			+ (comment.isEmpty() ? "" : " " + comment)
			+ " (" + config.getFileName() + ", " + (lineNum == -1 ? "unknown line" : "line " + lineNum) + ")";
	}

	public boolean debug() {
		return debug;
	}

	@Override
	public void invalidate() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean valid() {
		//noinspection ConstantValue
		return config != null && config.valid();
	}

	public @Nullable String getPath() {
		if (key == null)
			return null;
		if (parent == null)
			return key;
		@Nullable String path = parent.getPath();
		if (path == null)
			return key;
		return path + '.' + key;
	}

	@Override
	public @NotNull Node getCurrentNode() {
		return this;
	}

	/**
	 * @return The index of this node relative to the other children of this node's parent,
	 * or -1 if this node does not have a parent.
	 */
	int getIndex() {
		if (parent == null)
			return -1;

		int index = 0;
		for (Node node : parent) {
			if (node == this)
				return index;

			index++;
		}
		return -1;
	}

	/**
	 * Returns the node names in the path to this node from the config root.
	 * If this is not a section node, returns the path to its parent node.
	 *
	 * <p>
	 * Getting the path of node {@code z} in the following example would
	 * return an array with {@code w.x, y, z}.
	 * <pre>
	 *     w.x:
	 *      y:
	 *       z: true # this node
	 * </pre></p>
	 *
	 * @return The path to this node in the config file.
	 */
	public @NotNull String[] getPathSteps() {
		List<String> path = new ArrayList<>();
		Node node = this;

		while (node != null) {
			if (node.getKey() == null || node.getKey().isEmpty())
				break;

			path.add(0, node.getKey());
			node = node.getParent();
		}

		if (path.isEmpty())
			return new String[0];

		return path.toArray(new String[0]);
	}

	@Override
	public @Nullable String name() {
		return this.getKey();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Node other))
			return false;

		if (!Objects.equals(key, other.key)) {
			return false;
		}

		return Arrays.equals(getPathSteps(), other.getPathSteps());
	}

}
