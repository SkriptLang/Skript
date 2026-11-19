package org.skriptlang.skript.variables.storage;

import ch.njol.skript.variables.SerializedVariable;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.variables.VariableMap;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Saves variables directly from memory, to catch cases like an item renamed in place
 * by an addon. Every so often it goes through all global variables a few at a time, at the end of each tick,
 * serializes each one, and queues it for its storage only if it differs from what was saved for it last,
 * which {@link SavedVariableHashes} remembers. Values are serialized into a reused buffer and hashed there,
 * and only copied out when they changed, so checking the many that didn't allocates next to nothing.
 * <p>
 * Only used on the main thread, so whatever it queues is the current value, never an older one.
 */
final class VariableRefresher {


	private static final long TIME_BETWEEN_PASSES = TimeUnit.MINUTES.toNanos(5);
	private static final int CHECK_EVERY = 32; // how often to check the budget
	private static final Runnable NO_MAP_WRITE = () -> {
	};

	private SavedVariableHashes saved = new SavedVariableHashes(0); // what's saved, as of the last pass
	private @Nullable SavedVariableHashes next;                    // being built by the running pass
	private @Nullable Iterator<String> pass;                       // the running pass's cursor
	private boolean scheduled;                                     // whether nextPassAt is set
	private long nextPassAt;                                       // System.nanoTime() to start the next pass

	private final VariableMap globals; // a reference, we don't own it! so we should treat it as readonly
	private final BufferedSerializer serializer;
	private final Function<String, @Nullable VariableStorage> route;

	/**
	 * @param globals    the map holding every global variable
	 * @param serializer serializes values for saving, into a buffer it reuses
	 * @param route      finds the storage that saves a variable, or null if it isn't saved
	 */
	VariableRefresher(VariableMap globals,
	                  BufferedSerializer serializer,
	                  Function<String, @Nullable VariableStorage> route) {
		this.globals = globals;
		this.serializer = serializer;
		this.route = route;
	}

	/**
	 * Remembers that a storage already holds this value for a variable, so the refresh doesn't save it again.
	 * Called when a variable is loaded, and when a storage reports a change.
	 *
	 * @param name  the variable's name
	 * @param value the value the storage holds
	 */
	void recordSaved(String name, SerializedVariable.Value value) {
		long hash = SavedVariableHashes.hash(value);
		saved.put(name, hash);
		if (next != null)
			next.put(name, hash);
	}

	/**
	 * Forgets what was saved for a variable, because it was just set. Its new value is queued already, but isn't
	 * hashed, since that would slow down every set. Without this, a value set and then changed back in place to
	 * what was saved before would match the old hash, and never be saved.
	 * The next pass saves the variable again, once.
	 * <p>
	 * Never grows the tables, so an unsupported set from another thread can at worst lose this, not break them.
	 *
	 * @param name the variable's name
	 */
	void invalidate(String name) {
		saved.invalidate(name);
		if (next != null) // the running pass may have passed it already
			next.invalidate(name);
	}

	/**
	 * Starts a pass if one is due, then refreshes variables until the deadline.
	 *
	 * @param now      {@link System#nanoTime()} now
	 * @param deadline the {@link System#nanoTime()} to stop by
	 */
	void tick(long now, long deadline) {
		if (pass == null) {
			if (!scheduled) { // the first tick after loading. everything was just loaded, so wait a full interval
				nextPassAt = now + TIME_BETWEEN_PASSES;
				scheduled = true;
			}
			if (now - nextPassAt < 0) // compared by difference, since nanoTime can overflow
				return;
			next = new SavedVariableHashes((int) Math.min(globals.size(), Integer.MAX_VALUE / 2)); // a billion is overkill but better to have a max cap
			pass = globals.nameIterator();
		}
		int done = 0;
		while (pass.hasNext()) {
			if (++done % CHECK_EVERY == 0 && System.nanoTime() - deadline >= 0)
				return; // carry on next tick
			refresh(pass.next(), next);
		}
		saved = next; // names deleted since the last pass weren't put in next, so they're gone now
		next = null;
		pass = null;
		nextPassAt = System.nanoTime() + TIME_BETWEEN_PASSES;
	}

	/**
	 * Refreshes every variable now, with no deadline. Called once while shutting down, before the storages close.
	 */
	void refreshAll() {
		next = null;
		pass = null;
		SavedVariableHashes all = new SavedVariableHashes((int) Math.min(globals.size(), Integer.MAX_VALUE / 2));
		for (Iterator<String> it = globals.nameIterator(); it.hasNext(); )
			refresh(it.next(), all);
		saved = all;
	}

	private void refresh(String name, SavedVariableHashes into) {
		VariableStorage storage = route.apply(name);
		if (storage == null)
			return; // not saved: ephemeral, no storage takes it, its storage failed, or closed
		Object value = globals.getVariable(name);
		if (value == null)
			return; // deleted since the iterator passed it
		if (!serializer.serialize(value))
			return; // a type that can't be saved
		long hash = SavedVariableHashes.hash(serializer.type(), serializer.data(), serializer.offset(), serializer.length());
		if (!saved.matches(name, hash)) // copied out of the buffer only now, since few values change
			storage.queue().write(name, serializer.toValue(), NO_MAP_WRITE);
		into.put(name, hash);
	}

}
