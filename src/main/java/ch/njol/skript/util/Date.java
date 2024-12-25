package ch.njol.skript.util;

import java.util.TimeZone;

import org.jetbrains.annotations.Nullable;

import ch.njol.skript.SkriptConfig;
import ch.njol.yggdrasil.YggdrasilSerializable;

/**
 * @author Peter Güttinger
 */
public class Date implements Comparable<Date>, YggdrasilSerializable {
	
	/**
	 * Timestamp. Should always be in computer time/UTC/GMT+0.
	 */
	private long timestamp;
	
	public Date() {
		this(System.currentTimeMillis());
	}
	
	public Date(final long timestamp) {
		this.timestamp = timestamp;
	}
	
	public Date(final long timestamp, final TimeZone zone) {
		final long offset = zone.getOffset(timestamp);
		this.timestamp = timestamp - offset;
	}
	
	/**
	 * Get a new Date with the current time
	 *
	 * @return New date with the current time
	 */
	public static Date now() {
		return new Date(System.currentTimeMillis());
	}
	
	public Timespan difference(final Date other) {
		return new Timespan(Math.abs(timestamp - other.timestamp));
	}
	
	@Override
	public int compareTo(final @Nullable Date other) {
		final long d = other == null ? timestamp : timestamp - other.timestamp;
		return d < 0 ? -1 : d > 0 ? 1 : 0;
	}
	
	@Override
	public String toString() {
		return SkriptConfig.formatDate(timestamp);
	}
	
	/**
	 * Get the timestamp of this date
	 *
	 * @return The timestamp in milliseconds
	 */
	public long getTimestamp() {
		return timestamp;
	}
	
	/**
	 * Add a {@link Timespan} to this date
	 *
	 * @param span Timespan to add
	 */
	public void add(final Timespan span) {
		timestamp += span.getAs(Timespan.TimePeriod.MILLISECOND);
	}
	
	/**
	 * Subtract a {@link Timespan} from this date
	 *
	 * @param span Timespan to subtract
	 */
	public void subtract(final Timespan span) {
		timestamp -= span.getAs(Timespan.TimePeriod.MILLISECOND);
	}
	
	/**
	 * Get a new instance of this Date with the added timespan
	 *
	 * @param span Timespan to add to this Date
	 * @return New Date with the added timespan
	 */
	public Date plus(Timespan span) {
		return new Date(timestamp + span.getAs(Timespan.TimePeriod.MILLISECOND));
	}
	
	/**
	 * Get a new instance of this Date with the subtracted timespan
	 *
	 * @param span Timespan to subtract from this Date
	 * @return New Date with the subtracted timespan
	 */
	public Date minus(Timespan span) {
		return new Date(timestamp - span.getAs(Timespan.TimePeriod.MILLISECOND));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		return result;
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Date))
			return false;
		final Date other = (Date) obj;
		return timestamp == other.timestamp;
	}
	
}
