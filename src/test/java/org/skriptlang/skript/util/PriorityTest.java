package org.skriptlang.skript.util;

import org.junit.Assert;
import org.junit.Test;

public class PriorityTest {

	@Test
	public void testBase() {
		Priority base = Priority.base();

		Assert.assertTrue(base.before().isEmpty());
		Assert.assertTrue(base.after().isEmpty());

		// Different instances, but functionally equal
		Assert.assertEquals(base, Priority.base());
	}

	@Test
	public void testBefore() {
		Priority base = Priority.base();
		Priority before = Priority.before(base);

		Assert.assertTrue(before.before().contains(base));
		Assert.assertTrue(before.after().isEmpty());
		Assert.assertTrue(before.compareTo(base) < 0);
		Assert.assertTrue(base.compareTo(before) > 0);

		// Different instances, but functionally equal
		Assert.assertEquals(before, Priority.before(base));
	}

	@Test
	public void testAfter() {
		Priority base = Priority.base();
		Priority after = Priority.after(base);

		Assert.assertTrue(after.before().isEmpty());
		Assert.assertTrue(after.after().contains(base));
		Assert.assertTrue(after.compareTo(base) > 0);
		Assert.assertTrue(base.compareTo(after) < 0);

		// Different instances, but functionally equal
		Assert.assertEquals(after, Priority.after(base));
	}

	@Test
	public void testBoth() {
		Priority base = Priority.base();
		Priority before = Priority.before(base);
		Priority after = Priority.after(base);

		// 'before' should be before 'after'
		Assert.assertTrue(before.compareTo(after) < 0);
		// 'after' should be after 'before'
		Assert.assertTrue(after.compareTo(before) > 0);
	}

	@Test
	public void testComplex() {
		Priority base = Priority.base();

		Priority before = Priority.before(base);
		Priority afterBefore = Priority.after(before);
		// 'afterBefore' should be before 'base'
		Assert.assertTrue(afterBefore.compareTo(base) < 0);
		// 'base' should be after 'afterBefore'
		Assert.assertTrue(base.compareTo(afterBefore) > 0);

		Priority after = Priority.after(base);
		Priority beforeAfter = Priority.before(after);
		// 'beforeAfter' should be after 'base'
		Assert.assertTrue(beforeAfter.compareTo(base) > 0);
		// 'base' should be before 'beforeAfter'
		Assert.assertTrue(base.compareTo(beforeAfter) < 0);
	}

}
