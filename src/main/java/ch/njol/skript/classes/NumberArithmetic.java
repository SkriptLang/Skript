package ch.njol.skript.classes;

/**
 * @author Peter Güttinger
 */
@Deprecated
public class NumberArithmetic implements Arithmetic<Number, Number> {
	
	@Override
	public Number difference(final Number first, final Number second) {
		double result = Math.abs(first.doubleValue() - second.doubleValue());
		if (result == (long) result)
			return (long) result;
		return result;
	}
	
	@Override
	public Number add(final Number value, final Number difference) {
		double result = value.doubleValue() + difference.doubleValue();
		if (result == (long) result)
			return (long) result;
		return result;
	}
	
	@Override
	public Number subtract(final Number value, final Number difference) {
		double result = value.doubleValue() - difference.doubleValue();
		if (result == (long) result)
			return (long) result;
		return result;
	}

	@Override
	public Number multiply(Number value, Number multiplier) {
		double result = value.doubleValue() * multiplier.doubleValue();
		if (result == (long) result)
			return (long) result;
		return result;
	}

	@Override
	public Number divide(Number value, Number divider) {
		double result = value.doubleValue() / divider.doubleValue();
		if (result == (long) result)
			return (long) result;
		return result;
	}

	@Override
	public Number power(Number value, Number exponent) {
		double result = Math.pow(value.doubleValue(), exponent.doubleValue());
		if (result == (long) result)
			return (long) result;
		return result;
	}
	
}
