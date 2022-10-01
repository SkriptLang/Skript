package ch.njol.skript.classes;

import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.util.EnumUtils;
import org.eclipse.jdt.annotation.Nullable;

public class EnumClassInfo<T extends Enum<T>> extends ClassInfo<T> {

	/**
	 * @param c The class
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param defaultExpression The default expression of the type
	 */
	public EnumClassInfo(Class<T> c, String codeName, String languageNode, DefaultExpression<T> defaultExpression) {
		super(c, codeName);
		EnumUtils<T> enumUtils = new EnumUtils<>(c, languageNode);
		usage(enumUtils.getAllNames())
			.serializer(new EnumSerializer<>(c))
			.defaultExpression(defaultExpression)
			.parser(new Parser<T>() {
				@Override
				public @Nullable T parse(String s, ParseContext context) {
					return enumUtils.parse(s);
				}

				@Override
				public String toString(T o, int flags) {
					return enumUtils.toString(o, flags);
				}

				@Override
				public String toVariableNameString(T o) {
					return o.name();
				}
			});
	}

	/**
	 * @param c            The class
	 * @param codeName     The name used in patterns
	 * @param languageNode The language node of the type
	 */
	public EnumClassInfo(Class<T> c, String codeName, String languageNode) {
		this(c, codeName, languageNode, new EventValueExpression<>(c));
	}

}
