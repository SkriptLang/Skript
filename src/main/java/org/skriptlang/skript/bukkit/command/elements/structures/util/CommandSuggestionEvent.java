package org.skriptlang.skript.bukkit.command.elements.structures.util;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal event for managing context during "suggestions" entry evaluation in {@link SubCommandEntryData}.
 */
@ApiStatus.Internal
public class CommandSuggestionEvent extends Event {

	/**
	 * Map keyed by argument name.
	 */
	public Map<String, List<String>> suggestions = new HashMap<>();

	@Override
	@Contract("-> fail")
	public @NotNull HandlerList getHandlers() {
		throw new UnsupportedOperationException();
	}

}
