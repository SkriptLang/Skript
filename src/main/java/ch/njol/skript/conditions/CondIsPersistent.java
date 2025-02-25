package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Entity;

@Name("Is Persistent")
@Description({
	"Whether entities, players, or leaves are persistent.",
	"Persistence of entities is whether they are retained through server restarts.",
	"Persistence of leaves is whether they should decay when not connected to a log block within 6 meters.",
	"Persistence of players is if the player's playerdata should be saved when they leave the server. "
		+ "Players' persistence is reset back to 'true' when they join the server.",
	"Passengers inherit the persistence of their vehicle, meaning a persistent zombie put on a "
		+ "non-persistent chicken will become non-persistent. This does not apply to players.",
	"By default, all entities are persistent."
})
@Examples({
	"on spawn:",
		"\tif event-entity is persistent:",
			"\t\tmake event-entity not persistent"
})
@Since("INSERT VERSION")
public class CondIsPersistent extends PropertyCondition<Object> {

	static {
		register(CondIsPersistent.class, "persistent", "entities/blocks");
	}

	@Override
	public boolean check(Object object) {
		if (object instanceof Entity entity) {
			return entity.isPersistent();
		} else if (object instanceof Block block && block.getBlockData() instanceof Leaves leaves) {
			return leaves.isPersistent();
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "persistent";
	}

}
