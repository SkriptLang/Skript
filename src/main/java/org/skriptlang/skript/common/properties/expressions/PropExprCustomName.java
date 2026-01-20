package org.skriptlang.skript.common.properties.expressions;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Display Name")
@Description({
	"Represents the display name of a player, or the custom name of an item, entity, "
		+ "block, or inventory.",
	"",
	"<strong>Players:</strong> The name of the player that is displayed in messages. " +
		"This name can be changed freely and can include color codes, and is shared among all plugins (e.g. chat plugins will use the display name).",
	"",
	"<strong>Entities:</strong> The custom name of the entity. Can be changed, " +
		"which will also enable <em>custom name visibility</em> of the entity so name tag of the entity will be visible always.",
	"",
	"<strong>Items:</strong> The <em>custom</em> name of the item (not the Minecraft locale name). Can be changed.",
	"",
	"<strong>Inventories:</strong> The name/title of the inventory. " +
		"Changing name of an inventory means opening the same inventory with the same contents but with a different name to its current viewers.",
})
@Example("""
	on join:
		player has permission "name.red"
		set the player's display name to "&lt;red&gt;[admin] &lt;gold&gt;%name of player%"
	""")
@Since({
	"before 2.1",
	"2.2-dev20 (inventory name)",
	"2.4 (non-living entity support, changeable inventory name)"
})
@RelatedProperty("display name")
public class PropExprCustomName extends PropertyBaseExpression<ExpressionPropertyHandler<?,?>> {

	public static void register(SyntaxRegistry registry, Origin origin) {
		registry.register(SyntaxRegistry.EXPRESSION,
			PropertyExpression.infoBuilder(PropExprCustomName.class, Object.class, "(display|nick|chat|custom)[ ]name[s]", "objects", false)
				.origin(origin)
				.supplier(PropExprCustomName::new)
				.build());
	}

	@Override
	public Property<ExpressionPropertyHandler<?, ?>> getProperty() {
		return Property.DISPLAY_NAME;
	}

}

