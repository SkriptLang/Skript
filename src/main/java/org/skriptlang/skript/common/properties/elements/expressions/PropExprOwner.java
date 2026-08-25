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

@Name("Owner")
@Description("The owner of a tameable entity (i.e. horse or wolf).")
@Example("""
	set owner of last spawned wolf to player
	if the owner of last spawned wolf is player:
	""")
@Since("2.5")
@RelatedProperty("owner")
public class PropExprOwner extends PropertyBaseExpression<ExpressionPropertyHandler<?, ?>> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			PropertyExpression.infoBuilder(PropExprOwner.class, Object.class,
					"(owner|tamer)[s]", "objects", false)
				.supplier(PropExprOwner::new)
				.build());
	}

	@Override
	public Property<ExpressionPropertyHandler<?, ?>> getProperty() {
		return Property.OWNER;
	}

}
