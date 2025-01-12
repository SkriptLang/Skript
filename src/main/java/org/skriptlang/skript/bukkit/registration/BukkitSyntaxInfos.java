package org.skriptlang.skript.bukkit.registration;

import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptEvent.ListeningBehavior;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfosImpl.EventImpl;
import org.skriptlang.skript.registration.SyntaxInfo;

import java.util.Collection;

/**
 * A class containing the interfaces representing Bukkit-specific SyntaxInfo implementations.
 */
@ApiStatus.Experimental
public final class BukkitSyntaxInfos {

	private BukkitSyntaxInfos() { }

	/**
	 * A syntax info to be used for {@link SkriptEvent}s.
	 * It contains additional details including the Bukkit events represented along with documentation data.
	 * @param <E> The class providing the implementation of the SkriptEvent this info represents.
	 */
	public interface Event<E extends SkriptEvent> extends SyntaxInfo<E> {

		/**
		 * @param eventClass The Structure class the info will represent.
		 * @param name The name of the SkriptEvent.
		 * @return A Structure-specific builder for creating a syntax info representing <code>type</code>.
		 */
		static <E extends SkriptEvent> Builder<? extends Builder<?, E>, E> builder(
			Class<E> eventClass, String name
		) {
			return new EventImpl.BuilderImpl<>(eventClass, name);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@Contract("-> new")
		Builder<? extends Builder<?, E>, E> toBuilder();

		/**
		 * @return The listening behavior for the SkriptEvent. Determines when the event should trigger.
		 */
		ListeningBehavior listeningBehavior();

		/**
		 * @return The name of the {@link SkriptEvent}.
		 */
		String name();

		/**
		 * @return A documentation-friendly version of {@link #name()}.
		 */
		String id();

		/**
		 * @return Documentation data. Represents the version of the plugin in which a syntax was added.
		 * @see ch.njol.skript.doc.Since
		 */
		@Nullable String since();

		/**
		 * @return Documentation data. Used for identifying specific syntaxes in documentation.
		 * @see ch.njol.skript.doc.DocumentationId
		 */
		@Nullable String documentationId();

		/**
		 * @return Documentation data. A description of a syntax.
		 * @see ch.njol.skript.doc.Description
		 */
		Collection<String> description();

		/**
		 * @return Documentation data. Examples for using a syntax.
		 * @see ch.njol.skript.doc.Examples
		 */
		Collection<String> examples();

		/**
		 * @return Documentation data. Keywords are used by the search engine to provide relevant results.
		 * @see ch.njol.skript.doc.Keywords
		 */
		Collection<String> keywords();

		/**
		 * @return Documentation data. Plugins other than Skript that are required by a syntax.
		 * @see ch.njol.skript.doc.RequiredPlugins
		 */
		Collection<String> requiredPlugins();

		/**
		 * @return A collection of the classes representing the Bukkit events the {@link SkriptEvent} listens for.
		 */
		Collection<Class<? extends org.bukkit.event.Event>> events();

		/**
		 * An Event-specific builder is used for constructing a new Event syntax info.
		 * @see #builder(Class, String)
		 * @param <B> The type of builder being used.
		 * @param <E> The SkriptEvent class providing the implementation of the syntax info being built.
		 */
		interface Builder<B extends Builder<B, E>, E extends SkriptEvent> extends SyntaxInfo.Builder<B, E> {

			/**
			 * Sets the listening behavior the event will use.
			 * This determines when the event should trigger.
			 * By default, this is {@link ListeningBehavior#UNCANCELLED}.
			 * @param listeningBehavior The listening behavior to use.
			 * @return This builder.
			 * @see Event#listeningBehavior()
			 */
			@Contract("_ -> this")
			B listeningBehavior(ListeningBehavior listeningBehavior);

			/**
			 * Sets the "since" value the event's documentation will use.
			 * @param since The "since" value to use.
			 * @return This builder.
			 * @see Event#since()
			 */
			@Contract("_ -> this")
			B since(String since);

			/**
			 * Sets the documentation identifier the event's documentation will use.
			 * @param documentationId The documentation identifier to use.
			 * @return This builder.
			 * @see Event#documentationId()
			 */
			@Contract("_ -> this")
			B documentationId(String documentationId);

			/**
			 * Adds a description line to the event's documentation.
			 * @param description The description line to add.
			 * @return This builder.
			 * @see Event#description()
			 */
			@Contract("_ -> this")
			B addDescription(String description);

			/**
			 * Adds lines of description to the event's documentation.
			 * @param description The description lines to add.
			 * @return This builder.
			 * @see Event#description()
			 */
			@Contract("_ -> this")
			B addDescription(String... description);

			/**
			 * Adds lines of description to the event's documentation.
			 * @param description The description lines to add.
			 * @return This builder.
			 * @see Event#description()
			 */
			@Contract("_ -> this")
			B addDescription(Collection<String> description);

			/**
			 * Removes all description lines from the event's documentation.
			 * @return This builder.
			 * @see Event#description()
			 */
			@Contract("-> this")
			B clearDescription();

			/**
			 * Adds an example to the event's documentation.
			 * @param example The example to add.
			 * @return This builder.
			 * @see Event#examples()
			 */
			@Contract("_ -> this")
			B addExample(String example);

			/**
			 * Adds examples to the event's documentation.
			 * @param examples The examples to add.
			 * @return This builder.
			 * @see Event#examples()
			 */
			@Contract("_ -> this")
			B addExamples(String... examples);

			/**
			 * Adds examples to the event's documentation.
			 * @param examples The examples to add.
			 * @return This builder.
			 * @see Event#examples()
			 */
			@Contract("_ -> this")
			B addExamples(Collection<String> examples);

			/**
			 * Removes all examples from the event's documentation.
			 * @return This builder.
			 * @see Event#examples()
			 */
			@Contract("-> this")
			B clearExamples();

			/**
			 * Adds a keyword to the event's documentation.
			 * @param keyword The keyword to add.
			 * @return This builder.
			 * @see Event#keywords()
			 */
			@Contract("_ -> this")
			B addKeyword(String keyword);

			/**
			 * Adds keywords to the event's documentation.
			 * @param keywords The keywords to add.
			 * @return This builder.
			 * @see Event#keywords()
			 */
			@Contract("_ -> this")
			B addKeywords(String... keywords);

			/**
			 * Adds keywords to the event's documentation.
			 * @param keywords The keywords to add.
			 * @return This builder.
			 * @see Event#keywords()
			 */
			@Contract("_ -> this")
			B addKeywords(Collection<String> keywords);

			/**
			 * Removes all keywords from the event's documentation.
			 * @return This builder.
			 * @see Event#keywords()
			 */
			@Contract("-> this")
			B clearKeywords();

			/**
			 * Adds a required plugin to event's documentation.
			 * @param plugin The required plugin to add.
			 * @return This builder.
			 * @see Event#requiredPlugins()
			 */
			@Contract("_ -> this")
			B addRequiredPlugin(String plugin);

			/**
			 * Adds required plugins to the event's documentation.
			 * @param plugins The required plugins to add.
			 * @return This builder.
			 * @see Event#requiredPlugins()
			 */
			@Contract("_ -> this")
			B addRequiredPlugins(String... plugins);

			/**
			 * Adds required plugins to the event's documentation.
			 * @param plugins The required plugins to add.
			 * @return This builder.
			 * @see Event#requiredPlugins()
			 */
			@Contract("_ -> this")
			B addRequiredPlugins(Collection<String> plugins);

			/**
			 * Removes all required plugins from the event's documentation.
			 * @return This builder.
			 * @see Event#requiredPlugins()
			 */
			B clearRequiredPlugins();

			/**
			 * Adds an event to the event's documentation.
			 * @param event The event to add.
			 * @return This builder.
			 * @see Event#events()
			 */
			@Contract("_ -> this")
			B addEvent(Class<? extends org.bukkit.event.Event> event);

			/**
			 * Adds events to the event's documentation.
			 * @param events The events to add.
			 * @return This builder.
			 * @see Event#events()
			 */
			@Contract("_ -> this")
			B addEvents(Class<? extends org.bukkit.event.Event>[] events);

			/**
			 * Adds events to the event's documentation.
			 * @param events The events to add.
			 * @return This builder.
			 * @see Event#events()
			 */
			@Contract("_ -> this")
			B addEvents(Collection<Class<? extends org.bukkit.event.Event>> events);

			/**
			 * Removes all events from the event's documentation.
			 * @return This builder.
			 * @see Event#events()
			 */
			@Contract("-> this")
			B clearEvents();

			/**
			 * {@inheritDoc}
			 */
			@Override
			@Contract("-> new")
			Event<E> build();

		}

	}

	/**
	 * Fixes patterns in event by modifying every {@link ch.njol.skript.patterns.TypePatternElement} to be nullable.
	 */
	public static String fixPattern(String pattern) {
		char[] chars = pattern.toCharArray();
		StringBuilder stringBuilder = new StringBuilder();

		boolean inType = false;
		for (int i = 0; i < chars.length; i++) {
			char character = chars[i];
			stringBuilder.append(character);

			if (character == '%') {
				// toggle inType
				inType = !inType;

				// add the dash character if it's not already present
				// a type specification can have two prefix characters for modification
				if (inType && i + 2 < chars.length && chars[i + 1] != '-' && chars[i + 2] != '-')
					stringBuilder.append('-');
			} else if (character == '\\' && i + 1 < chars.length) {
				// Make sure we don't toggle inType for escape percentage signs
				stringBuilder.append(chars[i + 1]);
				i++;
			}
		}
		return stringBuilder.toString();
	}

}
