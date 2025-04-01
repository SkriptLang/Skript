package ch.njol.skript.config;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

/**
 * An empty line or a comment.
 * <p>
 * The subclass {@link InvalidNode} is used for invalid non-empty nodes, i.e. where a parsing error occurred.
 *
 * @author Peter Güttinger
 */
public class VoidNode extends Node {

	VoidNode(final String line, final String comment, final SectionNode parent, final int lineNum) {
		super(line.trim(), comment, parent, lineNum);
	}

	@SuppressWarnings("null")
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

	@Override
	public int hashCode() {
		// ensures that two void nodes can exist with the same parent as long as they are
		// at a different position
		return Objects.hash(Arrays.hashCode(getPathSteps()), comment, getIndex());
	}

}
