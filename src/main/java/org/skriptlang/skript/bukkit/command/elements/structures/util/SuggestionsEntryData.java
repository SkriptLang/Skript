package org.skriptlang.skript.bukkit.command.elements.structures.util;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import org.skriptlang.skript.lang.entry.util.TriggerEntryData;

/**
 * TriggerEntryData implementation for command suggestions.
 * Essentially just allows aliases for {@link #getKey()}.
 */
class SuggestionsEntryData extends TriggerEntryData {

	public SuggestionsEntryData() {
		super("suggestions", null, true);
	}

	@Override
	public boolean canCreateWith(Node node) {
		if (!(node instanceof SectionNode))
			return false;
		String key = node.getKey();
		if (key == null)
			return false;
		key = ScriptLoader.replaceOptions(key);
		return getKey().equalsIgnoreCase(key) || "tab completions".equalsIgnoreCase(key) || "tab complete".equalsIgnoreCase(key);
	}

}
