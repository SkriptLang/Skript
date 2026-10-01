package org.skriptlang.skript.common.properties.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RelatedProperty;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Content")
@Description({
	"The content of something, such as the pages of a book (Supports Skript's chat format).",
	"Note: In order to modify the pages of a new written book, you must have the title and author",
	"of the book set. Skript will do this for you, but if you want your own, please set those values."
})
@Example("set the content of the player's held item to \"This page was written with Skript!\"")
@Since("2.2-dev31, 2.7 (changers)")
@RelatedProperty("content")
public class PropExprContent extends PropertyBaseExpression<ExpressionPropertyHandler<?, ?>> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			PropertyExpression.infoBuilder(PropExprContent.class, Object.class,
					"[all [[of] the]] [book] (pages|content)", "objects", false)
				.supplier(PropExprContent::new)
				.build());
	}

	@Override
	public Property<ExpressionPropertyHandler<?, ?>> getProperty() {
		return Property.CONTENT;
	}

	@Override
	public boolean isSingle() {
		// PropertyBaseExpression only looks at the source expression, but content is multi-valued
		// (the pages of a book), just like the expression this replaces.
		return false;
	}

}
