package org.skriptlang.skript.common.properties.elements.expressions;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Title")
@Description("""
	Represents the title of something.
	""")
@Example("""
	set title of event-bossbar to "<red>hello!"
	""")
@Example("""
	on inventory click:
		if type of event-item is written book:
			set title of event-item to formatted "<obf>%random uuid%"
			send "Your book's title is now forever forgotten.." to player
	""")
@Since("INSERT VERSION")
@RelatedProperty("title")
public class PropExprTitle extends PropertyBaseExpression<ExpressionPropertyHandler<?,?>> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			PropertyExpression.infoBuilder(PropExprTitle.class, Object.class, "title[s]", "objects", false)
				.supplier(PropExprTitle::new)
				.build());
	}

	@Override
	public Property<ExpressionPropertyHandler<?, ?>> getProperty() {
		return Property.TITLE;
	}

}
