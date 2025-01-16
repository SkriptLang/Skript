package ch.njol.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

/**
 * @deprecated Use a use-case specific record.
 */
@Deprecated
public class Pair<T1, T2> implements Entry<T1, T2>, Cloneable, Serializable {
	@Serial
	private static final long serialVersionUID = 8296563685697678334L;

	protected @UnknownNullability T1 first;
	protected @UnknownNullability T2 second;
	
	public Pair() {
		first = null;
		second = null;
	}
	
	public Pair(@Nullable T1 first, @Nullable T2 second) {
		this.first = first;
		this.second = second;
	}
	
	public Pair(@NotNull Entry<T1, T2> entry) {
		this.first = entry.getKey();
		this.second = entry.getValue();
	}

	public @UnknownNullability T1 getFirst() {
		return first;
	}
	
	public void setFirst(@Nullable T1 first) {
		this.first = first;
	}

	public @UnknownNullability T2 getSecond() {
		return second;
	}
	
	public void setSecond(@Nullable T2 second) {
		this.second = second;
	}
	
	/**
	 * @return "first,second"
	 */
	@Override
	public String toString() {
		return first + "," + second;
	}
	
	/**
	 * Checks for equality with Entries to match {@link #hashCode()}
	 */
	@Override
	public final boolean equals(@Nullable Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Entry<?, ?> entry))
			return false;
		T1 first = this.first;
		T2 second = this.second;
		return (first == null ? entry.getKey() == null : first.equals(entry.getKey())) &&
				(second == null ? entry.getValue() == null : second.equals(entry.getValue()));
	}
	
	/**
	 * As defined by {@link Entry#hashCode()}
	 */
	@Override
	public final int hashCode() {
		return Objects.hash(first, second);
	}
	
	@Override
	public @UnknownNullability T1 getKey() {
		return first;
	}
	
	@Override
	public @UnknownNullability T2 getValue() {
		return second;
	}
	
	@Override
	public @UnknownNullability T2 setValue(@Nullable T2 value) {
		T2 old = second;
		second = value;
		return old;
	}
	
	/**
	 * @return a shallow copy of this pair
	 */
	@Override
	public Pair<T1, T2> clone() {
		return new Pair<>(this);
	}
	
}
