package org.skriptlang.skript.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class PriorityTest {

	@Test
	public void testBase() {
		Priority base = Priority.base();

		assertTrue(base.before().isEmpty());
		assertTrue(base.after().isEmpty());

		// Different instances, but functionally equal
		assertEquals(base, Priority.base());
	}

	@Test
	public void testBefore() {
		Priority base = Priority.base();
		Priority before = Priority.before(base);

		assertTrue(before.before().contains(base));
		assertTrue(before.after().isEmpty());
		assertTrue(before.compareTo(base) < 0);
		assertTrue(base.compareTo(before) > 0);

		// Different instances, but functionally equal
		assertEquals(before, Priority.before(base));
	}

	@Test
	public void testAfter() {
		Priority base = Priority.base();
		Priority after = Priority.after(base);

		assertTrue(after.before().isEmpty());
		assertTrue(after.after().contains(base));
		assertTrue(after.compareTo(base) > 0);
		assertTrue(base.compareTo(after) < 0);

		// Different instances, but functionally equal
		assertEquals(after, Priority.after(base));
	}

	@Test
	public void testBoth() {
		Priority base = Priority.base();
		Priority before = Priority.before(base);
		Priority after = Priority.after(base);

		// 'before' should be before 'after'
		assertTrue(before.compareTo(after) < 0);
		// 'after' should be after 'before'
		assertTrue(after.compareTo(before) > 0);
	}

	@Test
	public void testComplex() {
		Priority base = Priority.base();

		Priority before = Priority.before(base);
		Priority afterBefore = Priority.after(before);
		// 'afterBefore' should be before 'base'
		assertTrue(afterBefore.compareTo(base) < 0);
		// 'base' should be after 'afterBefore'
		assertTrue(base.compareTo(afterBefore) > 0);

		Priority after = Priority.after(base);
		Priority beforeAfter = Priority.before(after);
		// 'beforeAfter' should be after 'base'
		assertTrue(beforeAfter.compareTo(base) > 0);
		// 'base' should be before 'beforeAfter'
		assertTrue(base.compareTo(beforeAfter) < 0);
	}

}
