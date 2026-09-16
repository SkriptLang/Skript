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

@Name("Author")
@Description("The author of a book.")
@Example("""
	on book sign:
		broadcast "A new book has been created by %author of event-item%"
	""")
@Since("2.2-dev31")
@RelatedProperty("author")
public class PropExprAuthor extends PropertyBaseExpression<ExpressionPropertyHandler<?, ?>> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			PropertyExpression.infoBuilder(PropExprAuthor.class, Object.class,
					"[book] (author|writer|publisher)[s]", "objects", false)
				.supplier(PropExprAuthor::new)
				.build());
	}

	@Override
	public Property<ExpressionPropertyHandler<?, ?>> getProperty() {
		return Property.AUTHOR;
	}

}
