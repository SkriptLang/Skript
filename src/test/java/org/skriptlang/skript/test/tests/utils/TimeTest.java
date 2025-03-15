package org.skriptlang.skript.test.tests.utils;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.util.Time;
import org.junit.Test;

public class TimeTest extends SkriptJUnitTest {

	public void testTime(String parse, int hour, int minute) {
		Time time = Time.parse(parse);
		if ((parse.contains("am") && hour == 12)) {
			hour -= 12;
		} else if (parse.contains("pm")) {
			hour += 12;
		}
		assert time != null;
		assert time.getHour() == hour;
		assert time.getMinute() == minute;
	}

	public void testTime(String parse) {
		String strip = parse.replace("am", "");
		strip = strip.replace("pm", "");
		String[] sub = strip.split(":");
		int hour = Integer.parseInt(sub[0]);
		int minute = 0;
		if (sub.length == 2)
			minute = Integer.parseInt(sub[1]);
		testTime(parse, hour, minute);
	}

	@Test
	public void test() {
		for (int i = 1; i < 12; i++) {
			testTime(i + "am");
			testTime(i + ":30am");
			testTime(i + "pm");
			testTime(i + ":30pm");
		}
		for (int i = 0; i < 24; i++) {
			testTime(i + ":00");
			testTime(i + ":30");
		}
	}

}
