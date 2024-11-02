package ch.njol.skript.util;

import ch.njol.skript.SkriptConfig;
import ch.njol.yggdrasil.YggdrasilSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.TimeZone;

public class Date extends java.util.Date implements YggdrasilSerializable {

	/**
	 * Get a new Date with the current time
	 *
	 * @return New date with the current time
	 */
	public static Date now() {
		return new Date();
	}

	/**
	 * Converts a {@link java.util.Date} to a {@link Date}.
	 *
	 * @param date The {@link java.util.Date} to convert.
	 * @return The converted date.
	 */
	public static Date fromJavaDate(java.util.Date date) {
		return new Date(date.getTime());
	}

	/**
	 * Timestamp. Should always be in computer time/UTC/GMT+0.
	 */
	private long timestamp;

	/**
	 * Creates a new Date with the current time.
	 */
	public Date() {
		super(System.currentTimeMillis());
	}

	/**
	 * Creates a new Date with the provided timestamp.
	 *
	 * @param timestamp The timestamp in milliseconds.
	 */
	public Date(long timestamp) {
		super(timestamp);
	}

	/**
	 * Creates a new Date with the provided timestamp and timezone.
	 *
	 * @param timestamp The timestamp in milliseconds.
	 * @param zone The timezone to use.
	 */
	public Date(long timestamp, TimeZone zone) {
		super(timestamp - zone.getOffset(timestamp));
	}

	/**
	 * Add a {@link Timespan} to this date
	 *
	 * @param other Timespan to add
	 */
	public void add(Timespan other) {
		timestamp += other.getAs(Timespan.TimePeriod.MILLISECOND);
		setTime(timestamp);
	}

	/**
	 * Subtract a {@link Timespan} from this date
	 *
	 * @param other Timespan to subtract
	 */
	public void subtract(Timespan other) {
		timestamp -= other.getAs(Timespan.TimePeriod.MILLISECOND);
		setTime(timestamp);
	}

	/**
	 * Returns the difference between this date and another date as a {@link Timespan}.
	 *
	 * @param other The other date.
	 * @return The difference between the provided dates as a {@link Timespan}.
	 */
	public Timespan difference(Date other) {
		return new Timespan(Math.abs(timestamp - other.timestamp));
	}

	/**
	 * Get a new instance of this Date with the added timespan
	 *
	 * @param other Timespan to add to this Date
	 * @return New Date with the added timespan
	 */
	public Date plus(Timespan other) {
		return new Date(timestamp + other.getAs(Timespan.TimePeriod.MILLISECOND));
	}
	
	/**
	 * Get a new instance of this Date with the subtracted timespan
	 *
	 * @param other Timespan to subtract from this Date
	 * @return New Date with the subtracted timespan
	 */
	public Date minus(Timespan other) {
		return new Date(timestamp - other.getAs(Timespan.TimePeriod.MILLISECOND));
	}

	/**
	 * @deprecated Use {@link #getTime()} instead.
	 */
	@Deprecated
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + Long.hashCode(timestamp);
		return result;
	}
	
	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof java.util.Date other))
			return false;

		return timestamp == other.getTime();
	}

	@Override
	public int compareTo(java.util.Date other) {
		long delta = other == null ? timestamp : timestamp - other.getTime();
		return delta < 0 ? -1 : delta > 0 ? 1 : 0;
	}

	@Override
	public String toString() {
		return SkriptConfig.formatDate(timestamp);
	}

}
