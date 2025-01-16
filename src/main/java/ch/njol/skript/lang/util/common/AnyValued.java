package ch.njol.skript.lang.util.common;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.StringMode;
import com.sun.jdi.request.StepRequest;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.converter.Converters;

/**
 * A provider for anything with a value.
 * Anything implementing this (or convertible to this) can be used by the {@link ch.njol.skript.expressions.ExprValue}
 * property expression.
 *
 * @see AnyProvider
 */
public interface AnyValued<Type> extends AnyProvider {

	/**
	 * @return This thing's value
	 */
	@UnknownNullability
	Type value();

	default <Converted> Converted convertedValue(ClassInfo<Converted> expected) {
		Type value = value();
		if (value == null)
			return null;
		if (expected.getC().isInstance(value))
			return expected.getC().cast(value);

		// For strings, it is probably better to use toString/Parser in either
		// direction, instead of a converter

		if (expected.getC() == String.class)
			//noinspection unchecked
			return (Converted) Classes.toString(value, StringMode.MESSAGE);
		if (value instanceof String string
			&& expected.getParser() != null
			&& expected.getParser().canParse(ParseContext.CONFIG)) {
			return expected.getParser().parse(string, ParseContext.CONFIG);
		}

		return Converters.convert(value, expected.getC());
	}

	/**
	 * This is called before {@link #changeValue(Object)}.
	 * If the result is false, setting the value will never be attempted.
	 *
	 * @return Whether this supports being set
	 */
	default boolean supportsValueChange() {
		return false;
	}

	/**
	 * The behaviour for changing this thing's value, if possible.
	 * If not possible, then {@link #supportsValueChange()} should return false and this
	 * may throw an error.
	 *
	 * @param value The new value
	 * @throws UnsupportedOperationException If this is impossible
	 */
	default void changeValue(Type value) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 * @return The type of values this accepts (or provides)
	 */
	Class<Type> valueType();

	/**
	 * A default implementation of 'resetting' the value (setting it to null).
	 * Implementations should override this if different behaviour is required.
	 *
	 * @throws UnsupportedOperationException If changing is not supported
	 */
	default void resetValue() throws UnsupportedOperationException {
		this.changeValueSafely(null);
	}

	/**
	 * This method can be overridden to filter out bad values (e.g. null, objects of the wrong type, etc.)
	 * and make sure {@link #changeValue(Object)} is not called with a bad parameter.
	 *
	 * @param value The (unchecked) new value
	 */
	default void changeValueSafely(Object value) throws UnsupportedOperationException {
		Class<Type> typeClass = this.valueType();
		ClassInfo<? super Type> classInfo = Classes.getSuperClassInfo(typeClass);
		if (value == null) {
			this.changeValue(null);
		} else if (typeClass == String.class) {
			this.changeValue(typeClass.cast(Classes.toString(value, StringMode.MESSAGE)));
		} else if (value instanceof String string
			&& classInfo.getParser() != null
			&& classInfo.getParser().canParse(ParseContext.CONFIG)) {
			Type convert = (Type) classInfo.getParser().parse(string, ParseContext.CONFIG);
			this.changeValue(convert);
		} else {
			Type convert = Converters.convert(value, typeClass);
			this.changeValue(convert);
		}
	}

}
