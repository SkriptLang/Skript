package org.skriptlang.skript.bukkit.chat.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.ConvertedExpression;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Raw String")
@Description({
	"Returns the string without formatting (colors, decorations, etc.) and without stripping them from it.",
	"For example, <code>raw \"&aHello There!\"</code> would output <code>&aHello There!</code>"
})
@Example("send raw \"&aThis text is unformatted!\" to all players")
@Since("2.7")
public class ExprRawString extends SimplePropertyExpression<String, String> {

	private static final ConverterInfo<String, Component> RAW_STRING_CONVERTER =
		new ConverterInfo<>(String.class, Component.class, Component::text, 0);

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprRawString.class, String.class)
			.supplier(ExprRawString::new)
			.addPatterns("raw %strings%")
			.build());
	}

	@Override
	public String convert(String from) {
		return from;
	}

	@Override
	protected String getPropertyName() {
		return "raw";
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	@SafeVarargs
	public final @Nullable <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		for (Class<R> clazz : to) {
			if (Component.class.isAssignableFrom(clazz)) {
				//noinspection unchecked
				return (Expression<? extends R>) new ConvertedExpression<>(this, Component.class,
					RAW_STRING_CONVERTER);
			}
		}
		return super.getConvertedExpression(to);
	}

}
