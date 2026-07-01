package org.skriptlang.skript.common.properties.elements.expressions;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Viewers")
@Description("""
	Represents something that is viewing something.
	""")
@Example("""
	add {_player} to viewers of event-bossbar
	""")
@Example("""
	on inventory open:
		set {_mylist::*} to viewers of event-inventory
		send "%size of {_list::*}% other players are viewing this inventory!"
	""")
@Since("INSERT VERSION")
@RelatedProperty("viewers")
public class PropExprViewers extends PropertyBaseExpression<ExpressionPropertyHandler<?,?>> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			PropertyExpression.infoBuilder(PropExprViewers.class, Object.class, "viewer[s]", "objects", false)
				.supplier(PropExprViewers::new)
				.build());
	}

	@Override
	public Property<ExpressionPropertyHandler<?, ?>> getProperty() {
		return Property.VIEWERS;
	}

}
