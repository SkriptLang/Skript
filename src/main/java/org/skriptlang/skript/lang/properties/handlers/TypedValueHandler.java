package org.skriptlang.skript.lang.properties.handlers;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.ExprSubnodeValue;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.StringMode;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

/**
 * A handler for the `%classinfo% value of %property%` syntax
 * @param <Type> The type of the property holder
 * @param <ValueType> The type of the value returned
 */
@ApiStatus.Experimental
public interface TypedValueHandler<Type, ValueType> extends ExpressionPropertyHandler<Type, ValueType> {

	/**
	 * @return This thing's value
	 */
	@Override
	@Nullable ValueType convert(Type propertyHolder);

	default <Converted> Converted convert(Type propertyHolder, ClassInfo<Converted> expected) {
		ValueType value = convert(propertyHolder);
		if (value == null)
			return null;
		return ExprSubnodeValue.convertedValue(value, expected);
	}

	/**
	 * This method can be used to convert change values to ValueType (or null)
	 *
	 * @param value The (unchecked) new value
	 */
	default ValueType convertChangeValue(Object value) throws UnsupportedOperationException {
		Class<ValueType> typeClass = returnType();
		ClassInfo<? super ValueType> classInfo = Classes.getSuperClassInfo(typeClass);
		if (value == null) {
			return null;
		} else if (typeClass == String.class) {
			return typeClass.cast(Classes.toString(value, StringMode.MESSAGE));
		} else if (value instanceof String string
			&& classInfo.getParser() != null
			&& classInfo.getParser().canParse(ParseContext.CONFIG)) {
			return (ValueType) classInfo.getParser().parse(string, ParseContext.CONFIG);
		} else {
			return Converters.convert(value, typeClass);
		}
	}

}
