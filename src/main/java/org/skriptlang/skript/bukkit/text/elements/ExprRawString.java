package org.skriptlang.skript.bukkit.text.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Raw String")
@Description({
	"Returns the string without formatting (colors, decorations, etc.) and without stripping them from it.",
	"For example, <code>raw \"&aHello There!\"</code> would output <code>&aHello There!</code>"
})
@Example("send raw \"&aThis text is unformatted!\" to all players")
@Since("2.7")
public class ExprRawString extends SimplePropertyExpression<String, Object> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprRawString.class, Object.class)
			.supplier(ExprRawString::new)
			.addPatterns("raw %strings%")
			.build());
	}

	private boolean isComponent = true;

	@Override
	public Object convert(String from) {
		return isComponent ? Component.text(from) : from;
	}

	@Override
	public Class<?> getReturnType() {
		return isComponent ? Component.class : String.class;
	}

	@Override
	protected String getPropertyName() {
		return "raw";
	}

	@Override
	@SafeVarargs
	public final @Nullable <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		for (Class<R> clazz : to) {
			if (String.class.isAssignableFrom(clazz)) {
				ExprRawString converted = new ExprRawString();
				converted.setExpr(this.getExpr());
				converted.rawExpr = this.rawExpr;
				converted.isComponent = false;
				//noinspection unchecked
				return (Expression<? extends R>) converted;
			}
		}
		return super.getConvertedExpression(to);
	}

}
