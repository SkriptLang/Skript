package org.skriptlang.skript.test.tests.utils;

import ch.njol.skript.util.Date;
import org.junit.Test;

import java.util.TimeZone;

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
	public void testToJavaDate() {
		Date date = Date.now();
		java.util.Date javaDate = date.toJavaDate();
		assertEquals(date.getTimestamp(), javaDate.getTime());
	}

}
