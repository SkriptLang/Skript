package ch.njol.skript.config;

import org.jetbrains.annotations.Nullable;

/**
 * Represents any node that isn't invalid or a section in a simple config.
 */
public class SimpleNode extends Node {

	public SimpleNode(final String value, final String comment, final int lineNum, final SectionNode parent) {
		super(value, comment, parent, lineNum);
	}

	SimpleNode(String value, String comment, String[] comments, SectionNode parent, int lineNum) {
		super(value, comment, comments, parent, lineNum);
	}

	public SimpleNode(Config c) {
		super(c);
	}

	@Override
	String save_i() {
		return key;
	}

	public void set(String s) {
		key = s;
	}

	@Override
	public @Nullable Node get(String key) {
		return null;
	}

}
