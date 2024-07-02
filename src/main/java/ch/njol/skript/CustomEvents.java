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
package ch.njol.skript;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bukkit.event.Event;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.OptionSection;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.FileUtils;

public class CustomEvents {

	public static final OptionSection events = new OptionSection("events");

	private static boolean loaded;

	@SuppressWarnings("unchecked")
	public static void load(File file) {
		if (loaded)
			return;
		loaded = true;
		File eventsFile = new File(Skript.getInstance().getDataFolder(), "custom-events.sk");
		Config configuration = null;
		if (eventsFile.exists()) {
			try {
				configuration = new Config(eventsFile, false, false, ":");
			} catch (IOException e) {
				Skript.exception(e, "Failed to load custom-events.sk");
			}
		} else {
			try (ZipFile zip = new ZipFile(file)) {
				File saveTo = null;
				ZipEntry entry = zip.getEntry("custom-events.sk");
				if (entry != null) {
					File destination = new File(Skript.getInstance().getDataFolder(), entry.getName());
					if (!destination.exists())
						saveTo = destination;
				} if (saveTo != null) {
					try (InputStream in = zip.getInputStream(entry)) {
						FileUtils.save(in, saveTo);
					}
				}
			} catch (IOException e) {
				if (Skript.debug())
					Skript.exception(e);
			} finally {
				eventsFile = new File(Skript.getInstance().getDataFolder(), "custom-events.sk");
				try {
					configuration = new Config(eventsFile, false, false, ":");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if (configuration != null) {
			configuration.load(CustomEvents.class);
			// When we need to change values in the future of this file, bump the version node.
			String version = configuration.get("version");
			if (version != null && version.isEmpty() || !version.equalsIgnoreCase("1")) {
				Skript.warning("Your custom-events.sk config file is outdated. " +
						"Backup any changes you've made, and delete your custom-events.sk in the Skript folder to update it. " +
						"After, re-add any nodes you've changed.");
			}
			SectionNode section = (SectionNode) configuration.getMainNode().get("events");
			if (section != null && !section.isEmpty()) {
				for (Node node : section) {
					if (!(node instanceof SectionNode)) {
						Skript.error("Event node '" + node.getKey() + "' was not a section, ignoring.");
						continue;
					}
					SectionNode eventSection = (SectionNode) node;
					String name = node.getKey();
					if (name.isEmpty()) {
						Skript.error("Event node '" + node.getKey() + "' is empty/blank.");
						continue;
					}
					String enabled = eventSection.get("enabled", "true");
					if (enabled.equalsIgnoreCase("false"))
						continue;
					String description = eventSection.get("description", "custom event '" + name + "'");
					String pattern = eventSection.getValue("pattern");
					if (pattern == null) {
						Skript.warning("Event node '" + name + "' did not contain a 'pattern' value.");
						continue;
					}
					if (pattern.contains("%")) {
						SkriptLogger.setNode(eventSection.get("pattern"));
						Skript.warning("Event node '" + name + "' cannot contain any expressions in the pattern '" + pattern + "'");
						continue;
					}
					String eventClass = eventSection.getValue("class");
					if (eventClass == null) {
						Skript.warning("Event node '" + name + "' did not contain a 'class' value.");
						continue;
					}

					// Everything below this should be dedicated to the class node.
					SkriptLogger.setNode(eventSection.get("class"));
					Class<?> event;
					try {
						event = Class.forName(eventClass);
					} catch (Exception e) {
						if (Skript.logVeryHigh() || Skript.debug()) {
							Skript.exception(e, "Class '" + eventClass + "' for event node '" + name + "' had an exception loading.");
						} else {
							Skript.error("Failed to find class '" + eventClass + "' for event node '" + name + "'");
						}
						continue;
					}
					if (!(Event.class.isAssignableFrom(event))) {
						Skript.error("Class '" + eventClass + "' for event node '" + name + "' was not a valid Event class!");
						continue;
					}
					Skript.registerEvent(name, SimpleEvent.class, (Class<? extends Event>) event, pattern)
							.requiredPlugins("Custom Skript Event")
							.description(description);
				}
			}
		}
	}

}
