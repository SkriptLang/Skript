package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.lang.SkriptEvent.ListeningBehavior;
import ch.njol.skript.lang.SkriptEventInfo.ModernSkriptEventInfo;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.lang.structure.StructureInfo;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.util.Priority;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public sealed class SkriptEventInfo<E extends SkriptEvent> extends StructureInfo<E> permits ModernSkriptEventInfo {

	public Class<? extends Event>[] events;
	public final String name;
  
	private ListeningBehavior listeningBehavior;
	private String @Nullable [] description, examples, keywords, requiredPlugins;
	private @Nullable String since, documentationID;

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

		// default listening behavior should be dependent on config setting
		this.listeningBehavior = SkriptConfig.listenCancelledByDefault.value() ? ListeningBehavior.ANY : ListeningBehavior.UNCANCELLED;
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
  
	public String @Nullable [] getDescription() {
		return description;
	}

	public String @Nullable [] getExamples() {
		return examples;
	}

	public String @Nullable [] getKeywords() {
		return keywords;
	}

	public @Nullable String getSince() {
		return since;
	}

	public String @Nullable [] getRequiredPlugins() {
		return requiredPlugins;
	}

	public @Nullable String getDocumentationID() {
		return documentationID;
	}

	/*
	 * Registration API Compatibility
	 */

	/**
	 * Internal wrapper class for providing compatibility with the new Registration API.
	 */
	@ApiStatus.Internal
	@ApiStatus.Experimental
	public static final class ModernSkriptEventInfo<E extends SkriptEvent>
			extends SkriptEventInfo<E>
			implements BukkitSyntaxInfos.Event<E> {

		private final SyntaxOrigin origin;

		public ModernSkriptEventInfo(String name, String[] patterns, Class<E> eventClass, String originClassPath, Class<? extends Event>[] events) {
			super(name, patterns, eventClass, originClassPath, events);
			origin = SyntaxOrigin.of(Skript.getAddon(JavaPlugin.getProvidingPlugin(eventClass)));
		}

		@Override
		public Builder<? extends Builder<?, E>, E> builder() {
			return BukkitSyntaxInfos.Event.builder(type(), name())
				.origin(origin)
				.addPatterns(patterns())
				.priority(priority())
				.listeningBehavior(listeningBehavior())
				.since(since())
				.documentationId(id())
				.addDescription(description())
				.addExamples(examples())
				.addKeywords(keywords())
				.addRequiredPlugins(requiredPlugins())
				.addEvents(events());
		}

		@Override
		public SyntaxOrigin origin() {
			return origin;
		}

		@Override
		public Class<E> type() {
			return getElementClass();
		}

		@Override
		public E instance() {
			try {
				return type().getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException |
					 NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public @Unmodifiable Collection<String> patterns() {
			return List.of(getPatterns());
		}

		@Override
		public Priority priority() {
			return SyntaxInfo.COMBINED;
		}

		@Override
		public ListeningBehavior listeningBehavior() {
			return getListeningBehavior();
		}

		@Override
		public String name() {
			return getName();
		}

		@Override
		public String id() {
			return getId();
		}

		@Override
		public @Nullable String since() {
			return getSince();
		}

		@Override
		public @Nullable String documentationId() {
			return getDocumentationID();
		}

		@Override
		public Collection<String> description() {
			String[] description = getDescription();
			return description != null ? List.of(description) : List.of();
		}

		@Override
		public Collection<String> examples() {
			String[] examples = getExamples();
			return examples != null ? List.of(examples) : List.of();
		}

		@Override
		public Collection<String> keywords() {
			String[] keywords = getKeywords();
			return keywords != null ? List.of(keywords) : List.of();
		}

		@Override
		public Collection<String> requiredPlugins() {
			String[] requiredPlugins = getRequiredPlugins();
			return requiredPlugins != null ? List.of(requiredPlugins) : List.of();
		}

		@Override
		public Collection<Class<? extends Event>> events() {
			return List.of(events);
		}
	}

}
