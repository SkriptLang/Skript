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

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
public abstract class Node {
	
	@Nullable
	protected String key;
	
	protected String comment = "";
	
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
		assert comment.isEmpty() || comment.startsWith("#") : comment;
		this.comment = comment;
		debug = comment.equals("#DEBUG#");
		this.lineNum = lineNum;
		this.parent = parent;
		config = parent.getConfig();
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
	
	@SuppressWarnings("null")
	private final static Pattern linePattern = Pattern.compile("^((?:[^#]|##)*)(\\s*#(?!#).*)$");
	
	/**
	 * Splits a line into value and comment.
	 * <p>
	 * Whitespace is preserved (whitespace in front of the comment is added to the value), and any ## in the value are replaced by a single #. The comment is returned with a
	 * leading #, except if there is no comment in which case it will be the empty string.
	 * 
	 * @param line
	 * @return A pair (value, comment).
	 */
	public static NonNullPair<String, String> splitLine(final String line) {
		final Matcher m = linePattern.matcher(line);
		boolean matches = false;
		try {
			matches = line.contains("#") && m.matches();
		} catch (StackOverflowError e) { // Probably a very long line
			handleNodeStackOverflow(e, line);
		}
		if (matches)
			return new NonNullPair<>("" + m.group(1).replace("##", "#"), "" + m.group(2));
		return new NonNullPair<>("" + line.replace("##", "#"), "");
	}
	
	static void handleNodeStackOverflow(StackOverflowError e, String line) {
		Node n = SkriptLogger.getNode();
		SkriptLogger.setNode(null); // Avoid duplicating the which node error occurred in paranthesis on every error message
		
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
	
	public final String save() {
		return getIndentation() + save_i().replace("#", "##") + comment;
	}
	
	public void save(final PrintWriter w) {
		w.println(save());
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
	
//	/**
//	 * get a node via path:to:the:node. relative paths are possible by starting with a ':'; a double colon '::' will go up a node.<br/>
//	 * selecting the n-th node can be done with #n.
//	 *
//	 * @param path
//	 * @return the node at the given path or null if the path is invalid
//	 */
//	public Node getNode(final String path) {
//		return getNode(path, false);
//	}
//
//	public Node getNode(String path, final boolean create) {
//		Node n;
//		if (path.startsWith(":")) {
//			path = path.substring(1);
//			n = this;
//		} else {
//			n = config.getMainNode();
//		}
//		for (final String s : path.split(":")) {
//			if (s.isEmpty()) {
//				n = n.getParent();
//				if (n == null) {
//					n = config.getMainNode();
//				}
//				continue;
//			}
//			if (!(n instanceof SectionNode)) {
//				return null;
//			}
//			if (s.startsWith("#")) {
//				int i = -1;
//				try {
//					i = Integer.parseInt(s.substring(1));
//				} catch (final NumberFormatException e) {
//					return null;
//				}
//				if (i <= 0 || i > ((SectionNode) n).getNodeList().size())
//					return null;
//				n = ((SectionNode) n).getNodeList().get(i - 1);
//			} else {
//				final Node oldn = n;
//				n = ((SectionNode) n).get(s);
//				if (n == null) {
//					if (!create)
//						return null;
//					((SectionNode) oldn).getNodeList().add(n = new SectionNode(s, (SectionNode) oldn, "", -1));
//				}
//			}
//		}
//		return n;
//	}
	
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
	
}
