package ch.njol.skript.util;

import org.jetbrains.annotations.Nullable;

import ch.njol.yggdrasil.YggdrasilSerializable;

/**
 * @author Peter Güttinger
 */
public class Timeperiod implements YggdrasilSerializable {
	
	public final int start, end;
	
	public Timeperiod() {
		start = end = 0;
	}
	
	public Timeperiod(final int start, final int end) {
		this.start = (start + 24000) % 24000;
		this.end = (end + 24000) % 24000;
	}
	
	public Timeperiod(final int time) {
		start = end = (time + 24000) % 24000;
	}
	
	public boolean contains(final int time) {
		return start <= end ? (time >= start && time <= end) : (time <= end || time >= start);
	}
	
	public boolean contains(final Time t) {
		return contains(t.getTicks());
	}
	
	/**
	 * @return "start-end" or "start" if start == end
	 */
	@Override
	public String toString() {
		return "" + Time.toString(start) + (start == end ? "" : "-" + Time.toString(end));
	}
	
	@Override
	public int hashCode() {
		return start + end << 16;
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Timeperiod))
			return false;
		final Timeperiod other = (Timeperiod) obj;
		return (end == other.end && start == other.start);
	}
	
}
