/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.lang;

import java.util.Locale;

import org.skriptlang.skript.lang.structure.StructureInfo;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.SkriptAPIException;

public final class SkriptEventInfo<E extends SkriptEvent> extends StructureInfo<E> {

	public static final String EVENT_PRIORITY_SYNTAX = " [with priority (lowest|low|normal|high|highest|monitor)]";
	
	public Class<? extends Event>[] events;
	public final String name;
	
	private final String id;
	
	@Nullable
	private String[] description;
	@Nullable
	private String[] examples;
	@Nullable
	private String[] keywords;
	@Nullable
	private String since;
	@Nullable
	private String documentationID;
	@Nullable
	private String[] requiredPlugins;
	
	/**
	 * @param name Capitalised name of the event without leading "On" which is added automatically (Start the name with an asterisk to prevent this).
	 * @param patterns
	 * @param c The SkriptEvent's class
	 * @param originClassPath The class path for the origin of this event.
	 * @param events The Bukkit-Events this SkriptEvent listens to
	 */
	public SkriptEventInfo(String name, final String[] patterns, final Class<E> c, final String originClassPath, final Class<? extends Event>[] events) {
		super(patterns, c, originClassPath);
		assert name != null;
		assert patterns != null && patterns.length > 0;
		assert c != null;
		assert originClassPath != null;
		assert events != null && events.length > 0;
		
		for (int i = 0; i < events.length; i++) {
			for (int j = i + 1; j < events.length; j++) {
				if (events[i].isAssignableFrom(events[j]) || events[j].isAssignableFrom(events[i])) {
					if (events[i].equals(PlayerInteractAtEntityEvent.class)
							|| events[j].equals(PlayerInteractAtEntityEvent.class))
						continue; // Spigot seems to have an exception for those two events...
					
					throw new SkriptAPIException("The event " + name + " (" + c.getName() + ") registers with super/subclasses " + events[i].getName() + " and " + events[j].getName());
				}
			}
		}
		
		this.events = events;
		
		if (name.startsWith("*")) {
			this.name = name = "" + name.substring(1);
		} else {
			this.name = "On " + name;
		}
		
		// uses the name without 'on ' or '*'
		this.id = "" + name.toLowerCase(Locale.ENGLISH).replaceAll("[#'\"<>/&]", "").replaceAll("\\s+", "_");
	}
	
	/**
	 * Use this as {@link #description(String...)} to prevent warnings about missing documentation.
	 */
	public final static String[] NO_DOC = new String[0];
	
	/**
	 * Only used for Skript's documentation.
	 * 
	 * @param description
	 * @return This SkriptEventInfo object
	 */
	public SkriptEventInfo<E> description(final String... description) {
		assert this.description == null;
		this.description = description;
		return this;
	}
	
	/**
	 * Only used for Skript's documentation.
	 * 
	 * @param examples
	 * @return This SkriptEventInfo object
	 */
	public SkriptEventInfo<E> examples(final String... examples) {
		assert this.examples == null;
		this.examples = examples;
		return this;
	}

	/**
	 * Only used for Skript's documentation.
	 *
	 * @param keywords
	 * @return This SkriptEventInfo object
	 */
	public SkriptEventInfo<E> keywords(final String... keywords) {
		assert this.keywords == null;
		this.keywords = keywords;
		return this;
	}
	
	/**
	 * Only used for Skript's documentation.
	 * 
	 * @param since
	 * @return This SkriptEventInfo object
	 */
	public SkriptEventInfo<E> since(final String since) {
		assert this.since == null;
		this.since = since;
		return this;
	}

	/**
	 * A non critical ID remapping for syntax elements register using the same class multiple times.
	 *
	 * Only used for Skript's documentation.
	 *
	 * @param id
	 * @return This SkriptEventInfo object
	 */
	public SkriptEventInfo<E> documentationID(final String id) {
		assert this.documentationID == null;
		this.documentationID = id;
		return this;
	}

	/**
	 * Other plugin dependencies for this SkriptEvent.
	 *
	 * Only used for Skript's documentation.
	 *
	 * @param pluginNames
	 * @return This SkriptEventInfo object
	 */
	public SkriptEventInfo<E> requiredPlugins(final String... pluginNames) {
		assert this.requiredPlugins == null;
		this.requiredPlugins = pluginNames;
		return this;
	}

	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	@Nullable
	public String[] getDescription() {
		return description;
	}
	
	@Nullable
	public String[] getExamples() {
		return examples;
	}

	@Nullable
	public String[] getKeywords() {
		return keywords;
	}
	
	@Nullable
	public String getSince() {
		return since;
	}

	@Nullable
	public String[] getRequiredPlugins() {
		return requiredPlugins;
	}

	@Nullable
	public String getDocumentationID() {
		return documentationID;
	}
}
