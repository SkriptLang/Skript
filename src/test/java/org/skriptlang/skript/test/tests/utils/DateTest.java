package org.skriptlang.skript.test.tests.utils;

import ch.njol.skript.util.Date;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DateTest {

	@Test
	public void testNow() {
		Date now = Date.now();
		assertEquals(System.currentTimeMillis(), now.getTimestamp());
	}

	@Test
	public void testFromJavaDate() {
		java.util.Date javaDate = new java.util.Date();
		Date date = Date.fromJavaDate(javaDate);
		assertEquals(javaDate.getTime(), date.getTimestamp());
	}

	@Test
	public void testConstructorWithTimeZone() {
		Date date = new Date(0, java.util.TimeZone.getTimeZone("UTC+4:00"));
		assertEquals(4 * 60 * 60 * 1000, date.getTimestamp());
	}

	@Test
	public void testToJavaDate() {
		Date date = new Date();
		java.util.Date javaDate = date.toJavaDate();
		assertEquals(date.getTimestamp(), javaDate.getTime());
	}

}
