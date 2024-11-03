package org.skriptlang.skript.test.tests.utils;

import ch.njol.skript.util.Date;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DateTest {

	@Test
	public void testNow() {
		Date now = Date.now();
		assertEquals(System.currentTimeMillis(), now.getTime());
	}

	@Test
	public void testFromJavaDate() {
		java.util.Date javaDate = new java.util.Date();
		Date date = Date.fromJavaDate(javaDate);
		assertEquals(javaDate.getTime(), date.getTime());
	}

	@Test
	public void testEquals() {
		Date date1 = new Date(1000);
		Date date2 = new Date(1000);
		assertEquals(date1, date2);
	}

}
