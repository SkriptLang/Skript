package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A syntax element that restricts the events it can be used in.
 */
@FunctionalInterface
public interface EventRestrictedSyntax {

	/**
	 * Returns all supported events for this syntax element.
	 * <p>
	 * Before {@link SyntaxElement#init(Expression[], int, Kleenean, SkriptParser.ParseResult)} is called, checks
	 * to see if the current event is supported by this syntax element.
	 * If it is not, an error will be printed and the syntax element will not be initialised.
	 * </p>
	 *
	 * @return All supported event classes.
	 * @see CollectionUtils#array(Object[])
	 */
	Class<? extends Event>[] supportedEvents();

	/**
	 * Creates a readable list of the user-facing names of the given event classes.
	 * @param supportedEvents The classes of the events to list.
	 * @return A string containing the names of the events as a list: {@code "the on death event, the on explosion event, or the on player join event"}.
	 */
	static @NotNull String supportedEventsNames(Class<? extends Event>[] supportedEvents) {
		List<String> names = new ArrayList<>();

		for (SkriptEventInfo<?> eventInfo : Skript.getEvents()) {
			for (Class<? extends Event> eventClass : supportedEvents) {
				for (Class<? extends Event> event : eventInfo.events) {
					if (event.isAssignableFrom(eventClass)) {
						names.add("the %s event".formatted(eventInfo.getName().toLowerCase()));
					}
				}
			}
		}

		return StringUtils.join(names, ", ", " or ");
	}

}
