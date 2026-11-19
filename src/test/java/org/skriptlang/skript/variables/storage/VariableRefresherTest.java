package org.skriptlang.skript.variables.storage;

import ch.njol.skript.variables.SerializedVariable.Value;
import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.variables.VariableMap;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Tests {@link VariableRefresher} on its own: a plain map of variables, one storage whose queue shows what the
 * refresh queued, and {@link Box}es standing in for values that change in place.
 */
public class VariableRefresherTest {

	// later than any pass could be due, since passes are minutes apart
	private static final long A_DAY = TimeUnit.DAYS.toNanos(1);

	/**
	 * A value that changes in place, like an item renamed by an addon. Saved as its text.
	 */
	private static final class Box {
		String text;

		Box(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	/**
	 * A value whose type can't be saved.
	 */
	private static final class Unsaveable {
	}

	private VariableMap globals;
	private RecordingStorage storage;
	private VariableRefresher refresher;

	@Before
	public void setUp() {
		globals = VariableMap.concurrent();
		storage = new RecordingStorage();
		refresher = new VariableRefresher(globals, new ReusingSerializer(),
			name -> name.startsWith("unsaved::") ? null : storage);
	}

	/**
	 * Serializes like {@link #serialize(Object)}, but into one reused buffer, behind a few bytes that aren't part
	 * of the value and followed by whatever longer values left behind, like the real buffered serializer.
	 */
	private static final class ReusingSerializer implements BufferedSerializer {

		private static final int OFFSET = 3;
		private byte[] buffer = new byte[16];
		private String type = "";
		private int length;

		@Override
		public boolean serialize(Object value) {
			Value serialized = VariableRefresherTest.serialize(value);
			if (serialized == null)
				return false;
			if (buffer.length < OFFSET + serialized.data.length)
				buffer = Arrays.copyOf(buffer, OFFSET + serialized.data.length);
			System.arraycopy(serialized.data, 0, buffer, OFFSET, serialized.data.length);
			type = serialized.type;
			length = serialized.data.length;
			return true;
		}

		@Override
		public String type() {
			return type;
		}

		@Override
		public byte[] data() {
			return buffer;
		}

		@Override
		public int offset() {
			return OFFSET;
		}

		@Override
		public int length() {
			return length;
		}

	}

	private static Value serialize(Object value) {
		if (value instanceof Unsaveable)
			return null;
		return new Value("string", String.valueOf(value).getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Sets a variable as if it was loaded from the storage, so the storage holds it already.
	 */
	private void load(String name, Object value) {
		globals.setVariable(name, value);
		refresher.recordSaved(name, serialize(value));
	}

	/**
	 * The first tick, which only schedules the first pass.
	 */
	private void firstTick() {
		refresher.tick(System.nanoTime(), System.nanoTime() + A_DAY);
	}

	/**
	 * A tick once the next pass is due, with time for all of it.
	 */
	private void wholePass() {
		refresher.tick(System.nanoTime() + A_DAY, System.nanoTime() + A_DAY);
	}

	/**
	 * A tick whose deadline has passed already, so the pass only gets a few variables done.
	 */
	private void partOfAPass() {
		refresher.tick(System.nanoTime() + A_DAY, System.nanoTime());
	}

	/**
	 * Takes what was queued since the last call.
	 */
	private Map<String, String> queued() {
		Map<String, String> queued = new HashMap<>();
		RecordingStorage.changes(storage.queue().drain())
			.forEach((name, value) -> queued.put(name, new String(value.data, StandardCharsets.UTF_8)));
		return queued;
	}

	@Test
	public void theFirstTickOnlySchedulesAPass() {
		globals.setVariable("never::saved", "x");
		firstTick();
		assertEquals(Map.of(), queued());
		wholePass();
		assertEquals(Map.of("never::saved", "x"), queued());
	}

	@Test
	public void aPassQueuesValuesThatChangedInPlace() {
		Box box = new Box("old");
		load("box", box);
		load("other", "same");
		firstTick();
		box.text = "new";
		wholePass();
		assertEquals(Map.of("box", "new"), queued());
	}

	@Test
	public void aChangeIsOnlyQueuedByOnePass() {
		Box box = new Box("old");
		load("box", box);
		firstTick();
		box.text = "new";
		wholePass();
		assertEquals(Map.of("box", "new"), queued());
		wholePass();
		assertEquals(Map.of(), queued());
	}

	@Test
	public void aPassWithNothingChangedQueuesNothing() {
		for (int i = 0; i < 1000; i++)
			load("var::" + i, "value " + i);
		firstTick();
		wholePass();
		assertEquals(Map.of(), queued());
	}

	@Test
	public void aPassCarriesOnWhereTheLastTickStopped() {
		Box[] boxes = new Box[200];
		for (int i = 0; i < boxes.length; i++)
			load("box::" + i, boxes[i] = new Box("old"));
		firstTick();
		for (Box box : boxes)
			box.text = "new";

		Map<String, String> queued = new HashMap<>();
		partOfAPass();
		queued.putAll(queued());
		assertTrue("a tick past its deadline still gets some done", !queued.isEmpty());
		assertTrue("but not all of them", queued.size() < boxes.length);
		for (int ticks = 0; queued.size() < boxes.length; ticks++) {
			assertTrue("the pass should finish", ticks < boxes.length);
			partOfAPass();
			Map<String, String> more = queued();
			for (String name : more.keySet())
				assertFalse(name + " was queued twice in one pass", queued.containsKey(name));
			queued.putAll(more);
		}
		for (int i = 0; i < boxes.length; i++)
			assertEquals("new", queued.get("box::" + i));
	}

	@Test
	public void aPassNeverQueuesAnOlderValue() {
		for (int i = 0; i < 500; i++)
			load("var::" + i, "old");
		firstTick();
		partOfAPass(); // the pass has its cursor now
		queued();
		// resize the map's tables a few times, then change everything the pass may not have reached yet
		for (int i = 0; i < 100_000; i++)
			globals.setVariable("filler::" + i, "filler");
		for (int i = 0; i < 500; i++)
			globals.setVariable("var::" + i, "new");
		while (true) {
			Map<String, String> queued = queued();
			queued.forEach((name, value) -> {
				if (name.startsWith("var::"))
					assertEquals(name + " was queued with an older value", "new", value);
			});
			refresher.tick(System.nanoTime(), System.nanoTime() + A_DAY); // finishes the pass
			if (storage.queue().count() == 0)
				break;
		}
	}

	/**
	 * Sets a variable like the manager does: queues the new value, without hashing it, and invalidates.
	 */
	private void set(String name, Object value) {
		globals.setVariable(name, value);
		storage.queue().write(name, serialize(value), () -> {});
		refresher.invalidate(name);
	}

	@Test
	public void aValueSetThenChangedBackInPlaceIsQueued() {
		load("box", new Box("A"));
		firstTick();
		Box box = new Box("B");
		set("box", box);
		assertEquals(Map.of("box", "B"), queued());
		box.text = "A"; // what the storage held before the set, so it would match the old hash
		wholePass();
		assertEquals(Map.of("box", "A"), queued());
	}

	@Test
	public void aValueSetBehindARunningPassIsQueuedByTheNextOne() {
		Box[] boxes = new Box[200];
		for (int i = 0; i < boxes.length; i++)
			load("box::" + i, new Box("A"));
		firstTick();
		partOfAPass(); // passes some of them, putting their hash in the table it's building
		for (int i = 0; i < boxes.length; i++)
			set("box::" + i, boxes[i] = new Box("B"));
		queued();
		for (Box box : boxes)
			box.text = "A";
		Map<String, String> queued = new HashMap<>();
		refresher.tick(System.nanoTime(), System.nanoTime() + A_DAY); // finishes the pass
		queued.putAll(queued());
		wholePass();
		queued.putAll(queued());
		assertEquals(boxes.length, queued.size());
		queued.values().forEach(value -> assertEquals("A", value));
	}

	@Test
	public void aDeletedVariableIsForgottenOnceAPassEnds() {
		load("gone", "value");
		firstTick();
		globals.setVariable("gone", null);
		wholePass();
		assertEquals(Map.of(), queued());
		// set again, without being saved. had the old hash been kept, it would look saved already
		globals.setVariable("gone", "value");
		wholePass();
		assertEquals(Map.of("gone", "value"), queued());
	}

	@Test
	public void aValueSavedDuringAPassIsntQueuedByIt() {
		for (int i = 0; i < 500; i++)
			load("var::" + i, "old");
		firstTick();
		partOfAPass();
		queued();
		// a storage reports new values, for variables the pass may or may not have reached
		for (int i = 0; i < 500; i++)
			load("var::" + i, "reported");
		refresher.tick(System.nanoTime(), System.nanoTime() + A_DAY);
		assertEquals(Map.of(), queued());
		wholePass();
		assertEquals(Map.of(), queued());
	}

	@Test
	public void refreshingEverythingCatchesEveryChange() {
		Box box = new Box("old");
		load("box", box);
		box.text = "new";
		refresher.refreshAll(); // even before the first tick, like when the server stops right after starting
		assertEquals(Map.of("box", "new"), queued());
	}

	@Test
	public void refreshingEverythingTakesOverARunningPass() {
		Box[] boxes = new Box[200];
		for (int i = 0; i < boxes.length; i++)
			load("box::" + i, boxes[i] = new Box("old"));
		firstTick();
		for (Box box : boxes)
			box.text = "new";
		partOfAPass();
		refresher.refreshAll();
		Map<String, String> queued = queued();
		assertEquals(boxes.length, queued.size());
		// what it saw is saved now, so the pass it replaced doesn't carry on queueing
		refresher.tick(System.nanoTime(), System.nanoTime() + A_DAY);
		wholePass();
		assertEquals(Map.of(), queued());
	}

	@Test
	public void unsavedVariablesAreSkipped() {
		globals.setVariable("unsaved::x", "not saved anywhere");
		globals.setVariable("cant", new Unsaveable());
		firstTick();
		wholePass();
		assertEquals(Map.of(), queued());
		refresher.refreshAll();
		assertEquals(Map.of(), queued());
	}

}
