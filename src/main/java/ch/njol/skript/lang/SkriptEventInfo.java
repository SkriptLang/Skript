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

import ch.njol.skript.SkriptAPIException;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.structure.StructureInfo;
import ch.njol.skript.lang.SkriptEvent.ListeningBehavior;

import java.util.Locale;

public final class SkriptEventInfo<E extends SkriptEvent> extends StructureInfo<E> {

	public Class<? extends Event>[] events;
	public final String name;
  
	private ListeningBehavior listeningBehavior;
  
	@Nullable
	private String[] description, examples, keywords, requiredPlugins;

	@Nullable
	private String since, documentationID;

	private final String id;

	/**
	 * @param name Capitalised name of the event without leading "On" which is added automatically (Start the name with an asterisk to prevent this).
	 * @param patterns The Skript patterns to use for this event
	 * @param eventClass The SkriptEvent's class
	 * @param originClassPath The class path for the origin of this event.
	 * @param events The Bukkit-Events this SkriptEvent listens to
	 */
	public SkriptEventInfo(String name, String[] patterns, Class<E> eventClass, String originClassPath, Class<? extends Event>[] events) {
		super(patterns, eventClass, originClassPath);
		for (int i = 0; i < events.length; i++) {
			for (int j = i + 1; j < events.length; j++) {
				if (events[i].isAssignableFrom(events[j]) || events[j].isAssignableFrom(events[i])) {
					if (events[i].equals(PlayerInteractAtEntityEvent.class)
							|| events[j].equals(PlayerInteractAtEntityEvent.class))
						continue; // Spigot seems to have an exception for those two events...

					throw new SkriptAPIException("The event " + name + " (" + eventClass.getName() + ") registers with super/subclasses " + events[i].getName() + " and " + events[j].getName());
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

		// default listening behavior should be to listen to uncancelled events
		this.listeningBehavior = ListeningBehavior.UNCANCELLED;
	}
  
	/**
	 * Sets the default listening behavior for this SkriptEvent. If omitted, the default behavior is to listen to uncancelled events.
	 *
	 * @param listeningBehavior The listening behavior of this SkriptEvent.
	 * @return This SkriptEventInfo object
	 */
	public SkriptEventInfo<E> listeningBehavior(ListeningBehavior listeningBehavior) {
		this.listeningBehavior = listeningBehavior;
		return this;
	}

	/**
	 * Use this as {@link #description(String...)} to prevent warnings about missing documentation.
	 */
	public final static String[] NO_DOC = new String[0];

	/**
	 * Only used for Skript's documentation.
	 * 
	 * @param description The description of this event
	 * @return This SkriptEventInfo object
	 */
	public SkriptEventInfo<E> description(String... description) {
		this.description = description;
		return this;
	}

	/**
	 * Only used for Skript's documentation.
	 * 
	 * @param examples The examples for this event
	 * @return This SkriptEventInfo object
	 */
	public SkriptEventInfo<E> examples(String... examples) {
		this.examples = examples;
		return this;
	}

	/**
	 * Only used for Skript's documentation.
	 *
	 * @param keywords The keywords relating to this event
	 * @return This SkriptEventInfo object
	 */
	public SkriptEventInfo<E> keywords(String... keywords) {
		this.keywords = keywords;
		return this;
	}

	/**
	 * Only used for Skript's documentation.
	 * 
	 * @param since The version this event was added in
	 * @return This SkriptEventInfo object
	 */
	public SkriptEventInfo<E> since(String since) {
		assert this.since == null;
		this.since = since;
		return this;
	}

	/**
	 * A non-critical ID remapping for syntax elements register using the same class multiple times.
	 * <br>
	 * Only used for Skript's documentation.
	 *
	 * @param id The ID to use for this syntax element
	 * @return This SkriptEventInfo object
	 */
	public SkriptEventInfo<E> documentationID(String id) {
		assert this.documentationID == null;
		this.documentationID = id;
		return this;
	}

	/**
	 * Other plugin dependencies for this SkriptEvent.
	 * <br>
	 * Only used for Skript's documentation.
	 *
	 * @param pluginNames The names of the plugins this event depends on
	 * @return This SkriptEventInfo object
	 */
	public SkriptEventInfo<E> requiredPlugins(String... pluginNames) {
		this.requiredPlugins = pluginNames;
		return this;
	}


	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ListeningBehavior getListeningBehavior() {
		return listeningBehavior;
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
