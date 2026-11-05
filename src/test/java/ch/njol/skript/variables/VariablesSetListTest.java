package ch.njol.skript.variables;

import ch.njol.skript.lang.Variable;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.util.Pair;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Assume;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * Tests {@link Variables#setList} for what it does around {@link org.skriptlang.skript.variables.VariableMap#setList}:
 * case folding and falling back for nested keys, which can't be reached from a script
 * as a script's keyed values come from variables, whose keys are already lowercase,
 * and locking and queueing for global lists.
 * Also covers removing through {@link Variables#getVariableIterator}, which writes through the same paths.
 */
public class VariablesSetListTest extends SkriptJUnitTest {

	private static class ListEvent extends Event {

		private static final HandlerList HANDLERS = new HandlerList();

		@Override
		public @NotNull HandlerList getHandlers() {
			return HANDLERS;
		}

		@SuppressWarnings("unused")
		public static HandlerList getHandlerList() {
			return HANDLERS;
		}

	}

	private final Event event = new ListEvent();

	private static final String GLOBAL = "skript junit set list";

	@After
	public void cleanUp() {
		Variables.removeLocals(event);
		Variables.setVariable(GLOBAL + "::*", null, null, false);
	}

	private static Object getGlobal(String name) {
		return Variables.getVariable(GLOBAL + "::" + name, null, false);
	}

	private Object get(String name) {
		return Variables.getVariable(name, event, true);
	}

	@Test
	public void setsAList() {
		Variables.setList("l", new String[] {"1", "2"}, new Object[] {"a", "b"}, event, true);

		assertEquals("a", get("l::1"));
		assertEquals("b", get("l::2"));
	}

	@Test
	public void keysThatDifferOnlyByCaseAreOneKey() {
		Assume.assumeTrue(Variables.caseInsensitiveVariables);
		Variables.setList("L", new String[] {"B", "b", "A"}, new Object[] {"first", "second", "third"}, event, true);

		assertEquals("second", get("l::b"));
		assertEquals("second", get("l::B"));
		assertEquals("third", get("l::a"));
		Map<?, ?> list = (Map<?, ?>) get("l::*");
		assertNotNull(list);
		assertEquals(List.of("a", "b"), List.copyOf(list.keySet()));
	}

	@Test
	public void doesNotModifyTheArrays() {
		String[] keys = {"B", "A"};
		Object[] values = {"b", "a"};

		Variables.setList("l", keys, values, event, true);

		assertArrayEquals(new String[] {"B", "A"}, keys);
		assertArrayEquals(new Object[] {"b", "a"}, values);
	}

	@Test
	public void nestedKeysMakeSublists() {
		Variables.setList("l", new String[] {"a::b", "a::c", "d"}, new Object[] {"ab", "ac", "d"}, event, true);

		assertEquals("ab", get("l::a::b"));
		assertEquals("ac", get("l::a::c"));
		assertEquals("d", get("l::d"));
		Map<?, ?> sublist = (Map<?, ?>) get("l::a::*");
		assertNotNull("l::a::b was stored flat instead of in a sublist", sublist);
		assertEquals(2, sublist.size());
	}

	@Test
	public void listNameEndingInColonMatchesItsSplit() {
		// a: + :: + b is a:::b, which is {a}'s child :b, not a child of {a:}
		Variables.setList("a:", new String[] {"b", "c"}, new Object[] {"1", "2"}, event, true);

		assertEquals("1", get("a:::b"));
		assertEquals("2", get("a:::c"));
		Map<?, ?> list = (Map<?, ?>) get("a::*");
		assertNotNull("a:::b was stored under the wrong parent", list);
		assertEquals(List.of(":b", ":c"), List.copyOf(list.keySet()));
	}

	@Test
	public void keyStartingWithColonStaysInTheList() {
		Variables.setList("l", new String[] {":a", "b"}, new Object[] {"1", "2"}, event, true);

		assertEquals("1", get("l:::a"));
		Map<?, ?> list = (Map<?, ?>) get("l::*");
		assertNotNull(list);
		assertEquals(2, list.size());

		Variables.setVariable("l:::a", null, event, true);
		assertEquals(1, list.size()); // the tree has to agree on where it was
		assertNull(get("l:::a"));
	}

	@Test
	public void settingAListNameToAValueDoesNothing() {
		Variables.setVariable("l::1", "a", event, true);
		Variables.setVariable("l::*", "value", event, true);
		assertEquals("a", get("l::1"));
		assertEquals(1, ((Map<?, ?>) get("l::*")).size());

		Variables.setVariable(GLOBAL + "::1", "a", null, false);
		Variables.setVariable(GLOBAL + "::*", "value", null, false);
		assertEquals("a", getGlobal("1"));
		assertEquals(1, ((Map<?, ?>) getGlobal("*")).size());
	}

	@Test
	public void replacesTheOldList() {
		Variables.setList("l", new String[] {"1", "2", "x::y"}, new Object[] {"a", "b", "xy"}, event, true);
		Variables.setList("l", new String[] {"3"}, new Object[] {"c"}, event, true);

		assertNull(get("l::1"));
		assertNull(get("l::x::y"));
		assertEquals("c", get("l::3"));
	}

	@Test
	public void noKeysDeletesTheList() {
		Variables.setList("l", new String[] {"1"}, new Object[] {"a"}, event, true);
		Variables.setList("l", new String[0], new Object[0], event, true);

		assertNull(get("l::1"));
		assertNull(get("l::*"));
	}

	// --- global variables ---

	@Test
	public void setsAGlobalList() {
		Variables.setVariable(GLOBAL + "::old", "old", null, false);
		Variables.setVariable(GLOBAL + "::x::y", "nested", null, false);

		Variables.setList(GLOBAL, new String[] {"2", "B", "1"}, new Object[] {"two", "b", "one"}, null, false);

		assertNull(getGlobal("old"));
		assertNull(getGlobal("x::y"));
		assertEquals("one", getGlobal("1"));
		assertEquals("two", getGlobal("2"));
		Map<?, ?> list = (Map<?, ?>) getGlobal("*");
		assertNotNull(list);
		List<String> expected = Variables.caseInsensitiveVariables ? List.of("1", "2", "b") : List.of("1", "2", "B");
		assertEquals(expected, List.copyOf(list.keySet()));
	}

	@Test
	public void globalNestedKeysMakeSublists() {
		Variables.setList(GLOBAL, new String[] {"a::b", "c"}, new Object[] {"ab", "c"}, null, false);

		assertEquals("ab", getGlobal("a::b"));
		assertNotNull("a::b was stored flat instead of in a sublist", getGlobal("a::*"));
	}

	@Test
	public void noKeysDeletesAGlobalList() {
		Variables.setList(GLOBAL, new String[] {"1"}, new Object[] {"a"}, null, false);
		Variables.setList(GLOBAL, new String[0], new Object[0], null, false);

		assertNull(getGlobal("1"));
		assertNull(getGlobal("*"));
	}

	@Test
	public void iteratorRemoveDeletesTheReturnedElement() {
		for (boolean local : new boolean[] {true, false}) {
			String list = local ? "l" : GLOBAL;
			Event listEvent = local ? event : null;
			Variables.setList(list, new String[] {"1", "2", "3"}, new Object[] {"a", "b", "c"}, listEvent, local);

			Iterator<Pair<String, Object>> iterator = Variables.getVariableIterator(list + "::*", local, listEvent);
			assertEquals("1", iterator.next().getKey());
			assertTrue(iterator.hasNext()); // moves the iterator's lookahead onto 2
			iterator.remove();

			assertNull(Variables.getVariable(list + "::1", listEvent, local));
			assertEquals("b", Variables.getVariable(list + "::2", listEvent, local));
			assertEquals("c", Variables.getVariable(list + "::3", listEvent, local));
			assertThrows(IllegalStateException.class, iterator::remove); // only once per next
		}
	}

	@Test
	public void starKeysAreDropped() {
		Variables.setList("l", new String[] {"*", "1"}, new Object[] {"a", "b"}, event, true);

		Map<?, ?> list = (Map<?, ?>) get("l::*");
		assertNotNull(list);
		assertEquals(List.of("1"), List.copyOf(list.keySet()));
	}

	@Test
	public void unsavedGlobalsDontWaitForTheSaveOrderLock() throws InterruptedException {
		String name = Variable.EPHEMERAL_VARIABLE_TOKEN + GLOBAL;
		CountDownLatch locked = new CountDownLatch(1);
		CountDownLatch written = new CountDownLatch(1);
		AtomicBoolean finishedWhileHeld = new AtomicBoolean();
		Thread holder = new Thread(() -> {
			Variables.saveOrderLock.lock();
			try {
				locked.countDown();
				finishedWhileHeld.set(written.await(5, TimeUnit.SECONDS));
			} catch (InterruptedException ignored) {
			} finally {
				Variables.saveOrderLock.unlock();
			}
		});
		holder.start();
		assertTrue(locked.await(5, TimeUnit.SECONDS));

		Variables.setVariable(name + "::1", "a", null, false);
		Variables.setList(name, new String[] {"2"}, new Object[] {"b"}, null, false);
		written.countDown();
		holder.join(5000);

		try {
			assertTrue(finishedWhileHeld.get());
			assertEquals("b", Variables.getVariable(name + "::2", null, false));
		} finally {
			Variables.setVariable(name + "::*", null, null, false);
		}
	}

	@Test
	public void globalWritesWaitForTheSaveOrderLock() throws InterruptedException {
		Variables.setList(GLOBAL, new String[] {"1", "old"}, new Object[] {"a", "old"}, null, false);

		// hold the lock on another thread, as it's reentrant for this one
		CountDownLatch locked = new CountDownLatch(1);
		Thread holder = new Thread(() -> {
			Variables.saveOrderLock.lock();
			try {
				locked.countDown();
				Thread.sleep(100);
			} catch (InterruptedException ignored) {
			} finally {
				Variables.saveOrderLock.unlock();
			}
		});
		holder.start();
		assertTrue(locked.await(5, TimeUnit.SECONDS));

		// nothing gets queued for later anymore, so this has to be done once it returns
		Variables.setList(GLOBAL, new String[] {"2", "1"}, new Object[] {"b", "c"}, null, false);
		holder.join(5000);

		assertNull(getGlobal("old"));
		assertEquals("c", getGlobal("1"));
		assertEquals("b", getGlobal("2"));
	}

}
