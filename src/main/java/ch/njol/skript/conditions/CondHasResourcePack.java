package ch.njol.skript.conditions;

import org.bukkit.entity.Player;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;

@Name("Has Resource Pack")
@Description("Checks whether the given players have a server resource pack loaded. Please note that this can't detect " +
		"player's own resource pack, only the resource pack that sent by the server.")
@Examples("if the player has a resource pack loaded:")
@Since("2.4")
@RequiredPlugins("Paper 1.9 or newer")
public class CondHasResourcePack extends PropertyCondition<Player> {

	static {
		if (Skript.methodExists(Player.class, "hasResourcePack"))
			register(CondHasResourcePack.class, PropertyType.HAVE, "[a] resource pack [(loaded|installed)]", "players");
	}

	@Override
	public boolean check(Player player) {
		return player.hasResourcePack();
	}

	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.HAVE;
	}

	@Override
	protected String getPropertyName() {
		return "resource pack loaded";
	}

}
