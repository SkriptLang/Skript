package org.skriptlang.skript.common.elements.functions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.util.Date;
import ch.njol.util.Math2;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.common.function.DefaultFunction;
import org.skriptlang.skript.common.function.Parameter.Modifier;

import java.util.Calendar;

/**
 * Contains all generic time functions.
 */
public class TimeFunctions {

	static {
		SkriptAddon skript = Skript.instance();

		int[] fields = {
				Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH,
				Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND,
				Calendar.ZONE_OFFSET, Calendar.DST_OFFSET
		};
		int[] offsets = {
				0, -1, 0,
				0, 0, 0, 0,
				0, 0
		};
		double[] scale = {
				1, 1, 1,
				1, 1, 1, 1,
				1000 * 60, 1000 * 60
		};
		double[] relations = {
				1. / 12, 1. / 30,
				1. / 24, 1. / 60, 1. / 60, 1. / 1000,
				0, 0,
				0
		};

		Functions.register(DefaultFunction.builder(skript, "date", Date.class)
				.description("Creates a date from a year, month, and day, and optionally also from hour, minute, second and millisecond.",
						"A time zone and DST offset can be specified as well (in minutes), if they are left out the server's time zone and DST offset are used (the created date will not retain this information).")
				.examples("date(2014, 10, 1) # 0:00, 1st October 2014",
						"date(1990, 3, 5, 14, 30) # 14:30, 5th May 1990",
						"date(1999, 12, 31, 23, 59, 59, 999, -3*60, 0) # almost year 2000 in parts of Brazil (-3 hours offset, no DST)")
				.since("2.2")
				.parameter("year", Number.class)
				.parameter("month", Number.class)
				.parameter("day", Number.class)
				.parameter("hour", Number.class, Modifier.OPTIONAL)
				.parameter("minute", Number.class, Modifier.OPTIONAL)
				.parameter("second", Number.class, Modifier.OPTIONAL)
				.parameter("millisecond", Number.class, Modifier.OPTIONAL)
				.parameter("zone_offset", Number.class, Modifier.OPTIONAL)
				.parameter("dst_offset", Number.class, Modifier.OPTIONAL)
				.build(args -> {
					String[] paramNames = {"year", "month", "day", "hour", "minute", "second", "millisecond", "zone_offset", "dst_offset"};
					Calendar c = Calendar.getInstance();
					c.setLenient(true);
					double carry = 0;
					for (int i = 0; i < fields.length; i++) {
						Number n = args.getOrDefault(paramNames[i], i < 3 ? 0 : (i == 8 ? Double.NaN : 0));
						double value = n.doubleValue() * scale[i] + offsets[i] + carry;
						int v = (int) Math2.floor(value);
						carry = (value - v) * relations[i];
						//noinspection MagicConstant
						c.set(fields[i], v);
					}

					return new Date(c.getTimeInMillis(), c.getTimeZone());
				}));
	}

}
