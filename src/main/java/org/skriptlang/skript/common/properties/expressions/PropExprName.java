package org.skriptlang.skript.common.properties.expressions;

import ch.njol.skript.doc.*;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

@Name("Name")
@Description({
	"Represents the Minecraft account name of a player, or the custom name of an item, entity, "
		+ "block, inventory, gamerule, world, script or function.",
	"",
	"<strong>Players:</strong> The Minecraft account name of the player. Can't be changed.",
	"",
	"<strong>Entities:</strong> The custom name of the entity. Can be changed. But for living entities, " +
		"the players will have to target the entity to see its name tag. For non-living entities, the name will not be visible at all. To prevent this, use 'display name'.",
	"",
	"<strong>Items:</strong> The <em>custom</em> name of the item (not the Minecraft locale name). Can be changed.",
	"",
	"<strong>Inventories:</strong> The name/title of the inventory. " +
		"Changing name of an inventory means opening the same inventory with the same contents but with a different name to its current viewers.",
	"",
	"<strong>Gamerules:</strong> The name of the gamerule. Cannot be changed.",
	"",
	"<strong>Worlds:</strong> The name of the world. Cannot be changed.",
	"",
	"<strong>Scripts:</strong> The name of a script, excluding its file extension."
})
@Examples({
	"on join:",
	"\tplayer has permission \"name.red\"",
	"\tset the player's display name to \"&lt;red&gt;[admin] &lt;gold&gt;%name of player%\"",
	"set the name of the player's tool to \"Legendary Sword of Awesomeness\""
})
@Since({
	"before 2.1",
	"2.2-dev20 (inventory name)",
	"2.4 (non-living entity support, changeable inventory name)",
	"2.7 (worlds)"
})
@RelatedProperty("name")
public class PropExprName extends PropertyBaseExpression<ExpressionPropertyHandler<?,?>> {

	static {
		register(PropExprName.class , "name[s]", "objects");
	}

	@Override
	public Property<ExpressionPropertyHandler<?, ?>> getProperty() {
		return Property.NAME;
	}

}
