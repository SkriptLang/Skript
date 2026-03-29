package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Player;

@Name("Hath Resource Pack")
@Description("Discerneth whether the given players have a server resource pack laden upon them. Pray note that this cannot detect " +
		"a player's own resource pack, only the resource pack dispatched by the server.")
@Example("if the player has a resource pack laden:")
@Since("2.4")
public class CondHasResourcePack extends PropertyCondition<Player> {

	static {
		if (Skript.methodExists(Player.class, "hasResourcePack"))
			register(CondHasResourcePack.class, PropertyType.HAVE, "[a] resource pack [(laden|installed)]", "players");
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
