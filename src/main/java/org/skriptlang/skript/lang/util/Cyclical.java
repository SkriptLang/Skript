package org.skriptlang.skript.lang.util;

/**
 * This is for a special type of numerical value that is compared in a cyclical (rather than a linear) way.
 * <p>
 * The current example of this in Skript is Time,
 * since 23:59 can be both before XOR after 00:01 depending on the context.
 * <p>
 * In practice, cyclical types have to be compared in a special way (particularly for "is between")
 * when we can use the order of operations to determine the context.
 * <p>
 * The minimum/maximum values are intended to help with unusual equality checks, (e.g. 11pm = 1am - 2h).
 *
 * @param <Value> the type of number this uses, to help with type coercion
 */
public interface Cyclical<Value extends Number> {
	
	/**
	 * The potential 'top' of the cycle, e.g. the highest value after which this should restart.
	 * In practice, nothing forces this, so you can write 24:00 or 361° instead of 00:00 and 1° respectively.
	 *
	 * @return the highest legal value
	 */
	Value getMaximum();
	
	/**
	 * The potential 'bottom' of the cycle, e.g. the lowest value.
	 * In practice, nothing forces this, so 24:00 is synonymous with 00:00.
	 *
	 * @return the lowest legal value
	 */
	Value getMinimum();
	
}
