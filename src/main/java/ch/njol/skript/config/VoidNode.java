package ch.njol.skript.config;

import org.jetbrains.annotations.Nullable;

/**
 * An empty line or a comment.
 * <p>
 * The subclass {@link InvalidNode} is used for invalid non-empty nodes, i.e. where a parsing error occurred.
 *
 * @author Peter Güttinger
 */
public class VoidNode extends Node {

	VoidNode(String line, String comment, SectionNode parent, int lineNum) {
		super(line.trim(), comment, parent, lineNum);
	}

	VoidNode(String line, String comment, String[] comments, SectionNode parent, int lineNum) {
		super(line.trim(), comment, comments, parent, lineNum);
	}

	@Override
	public String getKey() {
		return key;
	}

	public void set(final String s) {
		key = s;
	}

	@Override
	String save_i() {
		return key;
	}

	@Override
	public @Nullable Node get(String key) {
		return null;
	}

}
