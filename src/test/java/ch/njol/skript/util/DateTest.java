package ch.njol.skript.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DateTest {

	@Test
	public void testFromJavaDate() {
		java.util.Date javaDate = new java.util.Date();
		Date date = Date.fromJavaDate(javaDate);
		assertEquals(javaDate.getTime(), date.getTime());
	}

	@Test
	public void testEquals() {
		Date date1 = new Date(1000);
		java.util.Date date2 = new java.util.Date(1000);
		assertEquals(date1, date2);
	}

}
