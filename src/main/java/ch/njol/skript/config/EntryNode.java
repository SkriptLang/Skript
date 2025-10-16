package ch.njol.skript.config;

import ch.njol.skript.lang.util.common.AnyValued;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Map.Entry;

/**
 * A node which holds a value.
 *
 * <p>
 * A general overview of node structure is as follows.
 * <pre><code>
 * section node:
 * 	entry node: true
 * 	simple node
 * </code></pre>
 * </p>
 */
public class EntryNode extends Node implements Entry<String, String>, AnyValued<String> {

	private String value;

	public EntryNode(final String key, final String value, final String comment, final SectionNode parent, final int lineNum) {
		super(key, comment, parent, lineNum);
		this.value = value;
	}

	EntryNode(String key, String value, String comment, String[] comments, SectionNode parent, int lineNum) {
		super(key, comment, comments, parent, lineNum);
		this.value = value;
	}

	public EntryNode(final String key, final String value, final SectionNode parent) {
		super(key, parent);
		this.value = value;
	}

	@SuppressWarnings("null")
	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public @UnknownNullability String value() {
		return this.getValue();
	}

	@Override
	public String setValue(final @Nullable String v) {
		if (v == null)
			return value;
		final String r = value;
		value = v;
		return r;
	}

	@Override
	public void changeValue(String value) throws UnsupportedOperationException {
		this.setValue(value);
	}

	@Override
	public Class<String> valueType() {
		return String.class;
	}

	@Override
	public boolean supportsValueChange() {
		return false; // todo editable configs soon
	}

	@Override
	String save_i() {
		return key + config.getSaveSeparator() + value;
	}

	@Override
	public @Nullable Node get(String step) {
		return null;
	}

}
