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
		assertTrue(before.isBefore(base));
		assertTrue(base.isAfter(before));

		// Different instances, but functionally equal
		assertEquals(before, Priority.before(base));
	}

	@Test
	public void testAfter() {
		Priority base = Priority.base();
		Priority after = Priority.after(base);

		assertTrue(after.before().isEmpty());
		assertTrue(after.after().contains(base));
		assertTrue(after.isAfter(base));
		assertTrue(base.isBefore(after));

		// Different instances, but functionally equal
		assertEquals(after, Priority.after(base));
	}

	@Test
	public void testBoth() {
		Priority base = Priority.base();
		Priority before = Priority.before(base);
		Priority after = Priority.after(base);

		assertTrue(before.isBefore(after));
		assertTrue(after.isAfter(before));
	}

	@Test
	public void testComplex() {
		Priority base = Priority.base();

		Priority before = Priority.before(base);
		Priority afterBefore = Priority.after(before);

		assertTrue(afterBefore.isBefore(base));
		assertTrue(base.isAfter(afterBefore));

		Priority after = Priority.after(base);
		Priority beforeAfter = Priority.before(after);

		assertTrue(beforeAfter.isAfter(base));
		assertTrue(base.isBefore(beforeAfter));
	}

}