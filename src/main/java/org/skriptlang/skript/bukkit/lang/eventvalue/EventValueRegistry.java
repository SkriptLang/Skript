package org.skriptlang.skript.bukkit.lang.eventvalue;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.util.Registry;
import org.skriptlang.skript.util.ViewProvider;

import java.util.*;

/**
 * Registry and resolver for {@link EventValue} definitions.
 * <p>
 * Use this registry to register, unregister and resolve event values by identifier text or by
 * desired value type. Resolution prefers the closest matching event type and, optionally,
 * can fall back to default time state and/or allow value-type conversion.
 * <p>
 * Obtain an instance using {@code SkriptAddon#registry(EventValueRegistry.class)}.
 * <br>
 * Or an unmodifiable view using {@code Skript.instance().registry(EventValueRegistry.class)}.
 */
public interface EventValueRegistry extends Registry<EventValue<?, ?>>, ViewProvider<EventValueRegistry> {

	/**
	 * Creates an empty event value registry.
	 *
	 * @param skript the Skript instance
	 * @return a new empty event value registry
	 */
	static EventValueRegistry empty(Skript skript) {
		return new EventValueRegistryImpl(skript);
	}

	/**
	 * Registers a new {@link EventValue}.
	 *
	 * @param eventValue the event value to register
	 * @param <E> the event type
	 * @throws SkriptAPIException if another value with the same
	 * event class, value class, time, and identifier patterns already exists
	 */
	<E extends Event> void register(EventValue<E, ?> eventValue);

	/**
	 * Unregisters the given event value.
	 *
	 * @param eventValue the event value to unregister
	 * @return {@code true} if the value was removed
	 */
	boolean unregister(EventValue<?, ?> eventValue);

	/**
	 * Checks whether an equivalent event value is already registered.
	 *
	 * @param eventValue the event value to check for
	 * @return {@code true} if an equivalent event value is registered
	 */
	boolean isRegistered(EventValue<?, ?> eventValue);

	/**
	 * Checks whether a value for the exact event/value class and time is registered.
	 *
	 * @param eventClass the event class to check for
	 * @param valueClass the value class to check for
	 * @param time the time state to check for
	 * @return {@code true} if a value is registered for the given parameters
	 */
	boolean isRegistered(Class<? extends Event> eventClass, Class<?> valueClass, EventValue.Time time);

	/**
	 * Resolve an {@link EventValue} by identifier using {@link EventValue.Time#NOW} and {@link Flags#DEFAULT}.
	 *
	 * @param eventClass the event type to resolve for
	 * @param identifier user input that identifies the value
	 * @param <E> the event type
	 * @param <V> the expected value type
	 * @return a {@link Resolution} describing candidates or empty/error state
	 * @see #resolve(Class, String, EventValue.Time)
	 */
	<E extends Event, V> Resolution<E, V> resolve(Class<E> eventClass, String identifier);

	/**
	 * Resolve an {@link EventValue} by identifier for a specific time using {@link Flags#DEFAULT}.
	 *
	 * @param eventClass the event type to resolve for
	 * @param identifier user input that identifies the value
	 * @param time the time state
	 * @param <E> the event type
	 * @param <V> the expected value type
	 * @return a {@link Resolution} describing candidates or empty/error state
	 * @see #resolve(Class, String, EventValue.Time, Flags)
	 */
	<E extends Event, V> Resolution<E, V> resolve(Class<E> eventClass, String identifier, EventValue.Time time);

	/**
	 * Resolve an {@link EventValue} by identifier with explicit time and flags.
	 *
	 * @param eventClass the event type to resolve for
	 * @param identifier user input that identifies the value
	 * @param time the time state
	 * @param flags the resolver flags
	 * @param <E> the event type
	 * @param <V> the expected value type
	 * @return a {@link Resolution} describing candidates or empty/error state
	 */
	<E extends Event, V> Resolution<E, V> resolve(
		Class<E> eventClass,
		String identifier,
		EventValue.Time time,
		Flags flags
	);

	/**
	 * Resolves by desired value class using {@link EventValue.Time#NOW} and {@link Flags#DEFAULT}.
	 *
	 * @param eventClass the event type to resolve for
	 * @param valueClass the desired value type
	 * @param <E> the event type
	 * @param <V> the desired value type
	 * @return a {@link Resolution} describing candidates or empty/error state
	 */
	<E extends Event, V> Resolution<E, ? extends V> resolve(Class<E> eventClass, Class<V> valueClass);

	/**
	 * Resolves by desired value class for a specific time using {@link Flags#DEFAULT}.
	 *
	 * @param eventClass the event type to resolve for
	 * @param valueClass the desired value type
	 * @param time the time state
	 * @param <E> the event type
	 * @param <V> the desired value type
	 * @return a {@link Resolution} describing candidates or empty/error state
	 */
	<E extends Event, V> Resolution<E, ? extends V> resolve(
		Class<E> eventClass,
		Class<V> valueClass,
		EventValue.Time time
	);

	/**
	 * Resolves by desired value class with explicit time and flags.
	 *
	 * @param eventClass the event type to resolve for
	 * @param valueClass the desired value type
	 * @param time the time state
	 * @param flags the resolver flags
	 * @param <E> the event type
	 * @param <V> the desired value type
	 * @return a {@link Resolution} describing candidates or empty/error state
	 */
	<E extends Event, V> Resolution<E, ? extends V> resolve(
		Class<E> eventClass,
		Class<V> valueClass,
		EventValue.Time time,
		Flags flags
	);

	/**
	 * Resolves only exact value-class matches, choosing the nearest compatible event class.
	 *
	 * @param eventClass the event type to resolve for
	 * @param valueClass the exact value type to match
	 * @param time the time state
	 * @param <E> the event type
	 * @param <V> the value type
	 * @return a {@link Resolution} describing candidates or empty/error state
	 */
	<E extends Event, V> Resolution<E, V> resolveExact(
		Class<E> eventClass,
		Class<V> valueClass,
		EventValue.Time time
	);

	/**
	 * Returns all registered event values at all time states.
	 *
	 * @return an unmodifiable list of all registered event values
	 */
	@Override
	@Unmodifiable List<EventValue<?, ?>> elements();

	/**
	 * Returns a snapshot of event values for the given time state.
	 *
	 * @param time the time state
	 * @return an unmodifiable list of event values for the given time state
	 */
	@Unmodifiable List<EventValue<?, ?>> elements(EventValue.Time time);

	/**
	 * Returns a snapshot of the <i>direct</i> (subevents only) event values for the given event.
	 * <br>
	 * For example, getting the event values of {@link org.bukkit.event.entity.EntityDeathEvent}
	 * will return the event values registered under {@link org.bukkit.event.entity.EntityDeathEvent} and
	 * {@link org.bukkit.event.entity.PlayerDeathEvent}, but not {@link org.bukkit.event.entity.EntityEvent}.
	 * 
	 * @param event the event
	 * @return an unmodifiable list of event values for the given event
	 */
	@Unmodifiable <E extends Event> List<EventValue<? extends E, ?>> elements(Class<E> event);

	/**
	 * @return an unmodifiable view of this registry
	 */
	@Override
	default EventValueRegistry unmodifiableView() {
		return new EventValueRegistryImpl.UnmodifiableView(this);
	}

	/**
	 * Result of a registry resolve operation. May contain multiple candidates or be empty.
	 * When {@link #errored()} is {@code true}, at least one candidate failed validation
	 * with {@link EventValue.Validation#ABORT} and no result should be used.
	 *
	 * @param all all candidates found during resolution
	 * @param errored whether the resolution failed due to an abort validation
	 * @param <E> the event type
	 * @param <V> the value type
	 */
	record Resolution<E extends Event, V>(List<EventValue<E, V>> all, boolean errored) {

		/**
		 * Creates a successful resolution from candidates.
		 *
		 * @param eventValues the candidates
		 * @param <E> the event type
		 * @param <V> the value type
		 * @return a new resolution
		 */
		public static <E extends Event, V> Resolution<E, V> of(List<EventValue<E, V>> eventValues) {
			return new Resolution<>(eventValues, false);
		}

		/**
		 * Creates an empty, non-error resolution.
		 *
		 * @param <E> the event type
		 * @param <V> the value type
		 * @return an empty resolution
		 */
		public static <E extends Event, V> Resolution<E, V> empty() {
			return new Resolution<>(Collections.emptyList(), false);
		}

		/**
		 * Creates an error resolution (e.g., a candidate failed validation).
		 *
		 * @param <E> the event type
		 * @param <V> the value type
		 * @return an error resolution
		 */
		public static <E extends Event, V> Resolution<E, V> error() {
			return new Resolution<>(Collections.emptyList(), true);
		}

		/**
		 * @return {@code true} if at least one candidate exists
		 */
		public boolean successful() {
			return !all.isEmpty();
		}

		/**
		 * @return {@code true} if multiple candidates are available
		 */
		public boolean multiple() {
			return all.size() > 1;
		}

		/**
		 * @return the single candidate
		 * @throws IllegalStateException if the resolution is not unique
		 */
		public EventValue<E, V> unique() {
			if (all.size() != 1)
				throw new IllegalStateException("Resolution is not unique (size: " + all.size() + ")");
			return all.getFirst();
		}

		/**
		 * @return the single candidate or {@code null} if not unique
		 */
		public EventValue<E, V> uniqueOrNull() {
			if (all.size() != 1)
				return null;
			return all.getFirst();
		}

		/**
		 * @return the single candidate as an {@link Optional}, empty if not unique
		 */
		public Optional<EventValue<E, V>> uniqueOptional() {
			if (all.size() != 1)
				return Optional.empty();
			return Optional.of(all.getFirst());
		}

		/**
		 * @return any candidate
		 * @throws IllegalStateException if the resolution is empty
		 */
		public EventValue<E, V> any() {
			if (all.isEmpty())
				throw new IllegalStateException("Resolution is empty");
			return all.getFirst();
		}

		/**
		 * @return any candidate or {@code null} if none
		 */
		public EventValue<E, V> anyOrNull() {
			if (all.isEmpty())
				return null;
			return all.getFirst();
		}

		/**
		 * @return any candidate as an {@link Optional}, empty if none
		 */
		public Optional<EventValue<E, V>> anyOptional() {
			if (all.isEmpty())
				return Optional.empty();
			return Optional.of(all.getFirst());
		}

		/**
		 * @return number of candidates contained
		 */
		public int size() {
			return all.size();
		}

	}

	/**
	 * Flags used during event value resolution.
	 */
	enum Flag {

		/**
		 * If resolution fails for the requested time state, fall back to the default time state (NOW).
		 */
		FALLBACK_TO_DEFAULT_TIME_STATE,

		/**
		 * Allow converters to be used to satisfy the requested value type.
		 */
		ALLOW_CONVERSION

	}

	/**
	 * A set of {@link Flag}s.
	 *
	 * @param set the set of flags
	 */
	record Flags(Set<Flag> set) {

		/**
		 * Default flags: fall back to default time state and allow conversion.
		 */
		public static final Flags DEFAULT = new Flags(Collections.unmodifiableSet(EnumSet.allOf(Flag.class)));

		/**
		 * No flags.
		 */
		public static final Flags NONE = new Flags(Collections.unmodifiableSet(EnumSet.noneOf(Flag.class)));

		/**
		 * Creates a new flags set from a collection.
		 *
		 * @param flags the flags to include
		 * @return a new flags set
		 */
		public static Flags of(Collection<Flag> flags) {
			return new Flags(EnumSet.copyOf(flags));
		}

		/**
		 * Creates a new flags set from an array.
		 *
		 * @param flags the flags to include
		 * @return a new flags set
		 */
		public static Flags of(Flag... flags) {
			return new Flags(EnumSet.noneOf(Flag.class)).with(flags);
		}

		/**
		 * Checks whether a flag is present.
		 *
		 * @param flag the flag to check for
		 * @return {@code true} if the flag is present
		 */
		public boolean has(Flag flag) {
			return set.contains(flag);
		}

		/**
		 * Returns a new flags set with the given flags added.
		 *
		 * @param flags the flags to add
		 * @return a new flags set
		 */
		public Flags with(Flag... flags) {
			Set<Flag> newSet = EnumSet.copyOf(set);
			newSet.addAll(Arrays.asList(flags));
			return new Flags(newSet);
		}

		/**
		 * Returns a new flags set with the given flags removed.
		 *
		 * @param flags the flags to remove
		 * @return a new flags set
		 */
		public Flags without(Flag... flags) {
			Set<Flag> newSet = EnumSet.copyOf(set);
			Arrays.asList(flags).forEach(newSet::remove);
			return new Flags(newSet);
		}

	}

}
