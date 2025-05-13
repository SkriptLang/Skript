package ch.njol.skript.config;

import java.util.Map.Entry;

import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
public class EntryNode extends Node implements Entry<String, String> {

	private String value;

	public EntryNode(final String key, final String value, final String comment, final SectionNode parent, final int lineNum) {
		super(key, comment, parent, lineNum);
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
	public String setValue(final @Nullable String v) {
		if (v == null)
			return value;
		final String r = value;
		value = v;
		return r;
	}

	@Override
	String save_i() {
		return key + config.getSaveSeparator() + value;
	}

}
