package org.skriptlang.skript.bukkit.docs;

import com.google.common.collect.ImmutableList;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.docs.Documentable;
import org.skriptlang.skript.docs.DocumentationAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Describes {@link Event}s that may be required at runtime for the thing(s) using this Documentable to function.
 */
public class Events implements Documentable {

	/**
	 * @param events The events that may be required at runtime for functionality.
	 * @return A new Events
	 */
	@Contract("_ -> new")
	@SafeVarargs
	public static Events of(Class<? extends Event>... events) {
		return new Events(events);
	}

	private final Collection<Class<? extends Event>> events;

	private Events(Class<? extends Event>[] events) {
		this.events = ImmutableList.copyOf(events);
	}

	/**
	 * @return The events that may be required at runtime for functionality.
	 */
	public @Unmodifiable Collection<Class<? extends Event>> events() {
		return events;
	}

	@Override
	public void write(DocumentationAdapter adapter) {
		List<Map<String, String>> data = new ArrayList<>();
		getRelatedInfos(adapter.addon()).forEach(info -> {
			Map<String, String> map = new HashMap<>();
			String id = info.documentationId();
			if (id == null) {
				id = info.id();
			}
			map.put("id", id);
			map.put("name", info.name());
			data.add(map);
		});
		adapter.write("events", data);
	}

	/**
	 * @param addon The addon to extract {@link BukkitSyntaxInfos.Event}s from.
	 * @return A collection of syntax infos that may require one or more of the {@link #events()} at runtime.
	 */
	protected Collection<BukkitSyntaxInfos.Event<?>> getRelatedInfos(SkriptAddon addon) {
		return addon.syntaxRegistry().syntaxes(BukkitSyntaxInfos.Event.KEY)
			.stream()
			.filter(info -> info.events().stream().anyMatch(events::contains))
			.toList();
	}

	/**
	 * Internal class for handling legacy string-based representation of events.
	 */
	@ApiStatus.Internal
	public static class LegacyEvents extends Events {

		private final String[] eventNames;

		public LegacyEvents(String[] eventNames) {
			//noinspection unchecked
			super(new Class[0]);
			this.eventNames = eventNames;
		}

		@Override
		public Collection<BukkitSyntaxInfos.Event<?>> getRelatedInfos(SkriptAddon addon) {
			List<BukkitSyntaxInfos.Event<?>> candidates = new ArrayList<>();
			boolean found = false;
			for (String eventName : eventNames) {
				for (BukkitSyntaxInfos.Event<?> info : addon.syntaxRegistry().syntaxes(BukkitSyntaxInfos.Event.KEY)) {
					String infoName = info.name().toLowerCase(Locale.ENGLISH);
					if (infoName.startsWith("on ")) {
						infoName = infoName.substring(3);
					}
					if (infoName.equals(eventName.toLowerCase(Locale.ENGLISH)) || info.id().equals(eventName)) {
						found = true;
						candidates.add(info);
					} else if (eventName.equals(info.documentationId())) { // should be unique, this is an exact match
						found = true;
						candidates.clear();
						candidates.add(info);
						break;
					}
				}
				if (!found) {
					throw new IllegalArgumentException("No matching info found for event annotation: " + eventName);
				}
				found = false;
			}
			return candidates;
		}
	}

}
