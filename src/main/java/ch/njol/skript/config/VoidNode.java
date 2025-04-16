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

//	private final int initialLevel;
//	private final String initialIndentation;

	VoidNode(final String line, final String comment, final SectionNode parent, final int lineNum) {
		super("" + line.trim(), comment, parent, lineNum);
//		initialLevel = getLevel();
//		initialIndentation = "" + line.replaceFirst("\\S.*$", "");
	}

	@SuppressWarnings("null")
	@Override
	public String getKey() {
		return key;
	}

	public void set(final String s) {
		key = s;
	}

	// doesn't work reliably
//	@Override
//	protected String getIndentation() {
//		int levelDiff = getLevel() - initialLevel;
//		if (levelDiff >= 0) {
//			return StringUtils.multiply(config.getIndentation(), levelDiff) + initialIndentation;
//		} else {
//			final String ci = config.getIndentation();
//			String ind = initialIndentation;
//			while (levelDiff < 0 && ind.startsWith(ci)) {
//				levelDiff++;
//				ind = "" + ind.substring(ci.length());
//			}
//			return ind;
//		}
//	}

	@Override
	String save_i() {
		return "" + key;
	}

	@Override
	public @Nullable Node get(String key) {
		return null;
	}

}
