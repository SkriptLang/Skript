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

@Name("Color of")
@Description("""
	The <a href='#color'>color</a> of an item, entity, block, firework effect, or text display.
	This can also be used to color chat messages with "&lt;%color of ...%&gt;this text is colored!".
	Do note that firework effects support setting, adding, removing, resetting, and deleting; text displays support \
	setting and resetting; and items, entities, and blocks only support setting, and only for very few items/blocks.
	""")
@Example("""
	on click on wool:
		if event-block is tagged with minecraft tag "wool":
			message "This wool block is <%color of block%>%color of block%<reset>!"
			set the color of the block to black
	""")
@Since({"1.2", "2.10 (displays)", "2.16 (boss bars)"})
@RelatedProperty("color")
public class PropExprColor extends PropertyBaseExpression<ExpressionPropertyHandler<?, ?>> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			PropertyExpression.infoBuilder(PropExprColor.class, Object.class, "colo[u]r[s]", "objects", false)
				.supplier(PropExprColor::new)
				.build());
	}

	@Override
	public Property<ExpressionPropertyHandler<?, ?>> getProperty() {
		return Property.COLOR;
	}

}
