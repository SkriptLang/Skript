package ch.njol.skript.classes;

/**
 * Represents arithmetic for certain two types. Multiplication, division and
 * power of methods are optional and may throw UnsupportedOperationExceptions.
 * @param <A> the type of the absolute value
 * @param <R> the type of the relative value
 */
@Deprecated
public interface Arithmetic<A, R> {
	
	public R difference(A first, A second);
	
	public A add(A value, R difference);
	
	public A subtract(A value, R difference);
	
	public A multiply(A value, R multiplier);
	
	public A divide(A value, R divider);
	
	public A power(A value, R exponent);
	
}
