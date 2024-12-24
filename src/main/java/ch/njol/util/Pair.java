package ch.njol.util;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
public class Pair<T1, T2> implements Entry<T1, T2>, Cloneable, Serializable {
	private static final long serialVersionUID = 8296563685697678334L;
	
	@Nullable
	protected T1 first;
	@Nullable
	protected T2 second;
	
	public Pair() {
		first = null;
		second = null;
	}
	
	public Pair(final @Nullable T1 first, final @Nullable T2 second) {
		this.first = first;
		this.second = second;
	}
	
	public Pair(final Entry<T1, T2> e) {
		this.first = e.getKey();
		this.second = e.getValue();
	}
	
	@Nullable
	public T1 getFirst() {
		return first;
	}
	
	public void setFirst(final @Nullable T1 first) {
		this.first = first;
	}
	
	@Nullable
	public T2 getSecond() {
		return second;
	}
	
	public void setSecond(final @Nullable T2 second) {
		this.second = second;
	}
	
	/**
	 * @return "first,second"
	 */
	@Override
	public String toString() {
		return "" + first + "," + second;
	}
	
	/**
	 * Checks for equality with Entries to match {@link #hashCode()}
	 */
	@Override
	public final boolean equals(final @Nullable Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Entry))
			return false;
		final Entry<?, ?> other = (Entry<?, ?>) obj;
		final T1 first = this.first;
		final T2 second = this.second;
		return (first == null ? other.getKey() == null : first.equals(other.getKey())) &&
				(second == null ? other.getValue() == null : second.equals(other.getValue()));
	}
	
	/**
	 * As defined by {@link Entry#hashCode()}
	 */
	@Override
	public final int hashCode() {
		return Objects.hash(first, second);
	}
	
	@Override
	@Nullable
	public T1 getKey() {
		return first;
	}
	
	@Override
	@Nullable
	public T2 getValue() {
		return second;
	}
	
	@Override
	@Nullable
	public T2 setValue(final @Nullable T2 value) {
		final T2 old = second;
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
