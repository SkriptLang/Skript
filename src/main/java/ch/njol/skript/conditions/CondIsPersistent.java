package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Entity;

@Name("Be Persistent")
@Description({
	"Whether entities, players, or leaves do endure with persistence.",
	"Persistence of entities speaketh to whether they be retained through server restarts.",
	"Persistence of leaves is whether they ought to decay when unconnected to a log block within six metres.",
	"Persistence of players determineth if the player's data shall be preserved when they depart the server. "
		+ "Players' persistence is restored unto 'true' when they rejoin the server.",
	"Passengers do inherit the persistence of their vehicle, meaning a persistent zombie set upon a "
		+ "non-persistent chicken shall itself become non-persistent. This applieth not unto players.",
	"By default, all entities art persistent."
})
@Example("""
	on spawn:
		if event-entity is persistent:
			make event-entity not persistent
	""")
@Since("2.11")
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
