package ch.njol.skript.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptEvent.ListeningBehavior;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.Locale;

@Name("Event")
@Description("""
	The structure used for listening to events.
	
	Optionally allows specifying whether to listen to events that have been cancelled, \
	and allows specifying with which priority to listen to events. \
	Events are called in the following order of priorities.
	
	```
	lowest -> low -> normal -> high -> highest -> monitor
	```
	
	Modifying event-values or cancelling events is not supported when using the 'monitor' priority. \
	It should only be used for monitoring the outcome of an event.
	""")
@Example("""
	on load:
		broadcast "loading!"
	""")
@Example("""
	on join:
		if {first-join::%player's uuid%} is not set:
			set {first-join::%player's uuid%} to now
	""")
@Example("""
	cancelled block break:
		send "<red>You can't break that here" to player
	""")
@Example("""
	on join with priority lowest:
		# called first
	
	on join:
		# called second

	on join with priority highest:
		# called last
	""")
@Since({"1.0", "2.6 (per-event priority)", "2.9 (listening to cancellable events)"})
public class StructEvent extends Structure {

	static {
		Skript.registerStructure(StructEvent.class,
				"[on] [:uncancelled|:cancelled|any:(any|all)] <.+> [priority:with priority (:(lowest|low|normal|high|highest|monitor))]");
	}

	private SkriptEvent event;

	@Override
	@SuppressWarnings("ConstantConditions")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, @Nullable EntryContainer entryContainer) {
		String expr = parseResult.regexes.get(0).group();

		EventData data = getParser().getData(EventData.class);
		// ensure there's no leftover data from previous parses
		data.clear();

		if (parseResult.hasTag("uncancelled")) {
			data.behavior = ListeningBehavior.UNCANCELLED;
		} else if (parseResult.hasTag("cancelled")) {
			data.behavior = ListeningBehavior.CANCELLED;
		} else if (parseResult.hasTag("any")) {
			data.behavior = ListeningBehavior.ANY;
		}

		if (parseResult.hasTag("priority")) {
			String lastTag = parseResult.tags.get(parseResult.tags.size() - 1);
			data.priority = EventPriority.valueOf(lastTag.toUpperCase(Locale.ENGLISH));
		}

		assert entryContainer != null;
		event = SkriptEvent.parse(expr, entryContainer.getSource(), null);

		// cleanup after ourselves
		data.clear();
		return event != null;
	}

	@Override
	public boolean preLoad() {
		getParser().setCurrentStructure(event);
		return event.preLoad();
	}

	@Override
	public boolean load() {
		getParser().setCurrentStructure(event);
		return event.load();
	}

	@Override
	public boolean postLoad() {
		getParser().setCurrentStructure(event);
		return event.postLoad();
	}

	@Override
	public void unload() {
		event.unload();
	}

	@Override
	public void postUnload() {
		event.postUnload();
	}

	@Override
	public Priority getPriority() {
		return event.getPriority();
	}

	public SkriptEvent getSkriptEvent() {
		return event;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return this.event.toString(event, debug);
	}

	static {
		ParserInstance.registerData(EventData.class, EventData::new);
	}

	public static class EventData extends ParserInstance.Data {

		@Nullable
		private EventPriority priority;
		@Nullable
		private ListeningBehavior behavior;

		public EventData(ParserInstance parserInstance) {
			super(parserInstance);
		}

		@Nullable
		public EventPriority getPriority() {
			return priority;
		}

		/**
		 * @return the listening behavior that should be used for the event. Null indicates that the user did not specify a behavior.
		 */
		@Nullable
		public ListeningBehavior getListenerBehavior() {
			return behavior;
		}
      
    	/**
		 * Clears all event-specific data from this instance.
		 */
		public void clear() {
			priority = null;
      		behavior = null;
		}

	}

}
