package ch.njol.skript.config;

/**
 * @author Peter Güttinger
 */
public class SimpleNode extends Node {
	
	public SimpleNode(final String value, final String comment, final int lineNum, final SectionNode parent) {
		super(value, comment, parent, lineNum);
	}
	
	public SimpleNode(final Config c) {
		super(c);
	}
	
	@SuppressWarnings("null")
	@Override
	String save_i() {
		return key;
	}
	
	public void set(final String s) {
		key = s;
	}
	
}
