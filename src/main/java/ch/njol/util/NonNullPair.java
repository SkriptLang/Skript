package ch.njol.util;

/**
 * @deprecated Use a use-case specific record.
 */
@Deprecated
public class NonNullPair<T1, T2> extends Pair<T1, T2> {
	private static final long serialVersionUID = 820250942098905541L;
	
	public NonNullPair(final T1 first, final T2 second) {
		this.first = first;
		this.second = second;
	}
	
	public NonNullPair(final NonNullPair<T1, T2> other) {
		first = other.first;
		second = other.second;
	}
	
	@Override
	@SuppressWarnings("null")
	public T1 getFirst() {
		return first;
	}
	
	@SuppressWarnings("null")
	@Override
	public void setFirst(final T1 first) {
		this.first = first;
	}
	
	@Override
	@SuppressWarnings("null")
	public T2 getSecond() {
		return second;
	}
	
	@SuppressWarnings("null")
	@Override
	public void setSecond(final T2 second) {
		this.second = second;
	}
	
	@SuppressWarnings("null")
	@Override
	public T1 getKey() {
		return first;
	}
	
	@SuppressWarnings("null")
	@Override
	public T2 getValue() {
		return second;
	}
	
	@SuppressWarnings("null")
	@Override
	public T2 setValue(final T2 value) {
		final T2 old = second;
		second = value;
		return old;
	}
	
	/**
	 * @return a shallow copy of this pair
	 */
	@Override
	public NonNullPair<T1, T2> clone() {
		return new NonNullPair<>(this);
	}
	
}
