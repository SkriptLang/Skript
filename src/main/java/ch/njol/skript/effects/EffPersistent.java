package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Persistent")
@Description({
	"Make entities, players, or leaves be persistent.",
	"Persistence of entities is whether they are retained through server restarts.",
	"Persistence of leaves is whether they should decay when not connected to a log block within 6 meters.",
	"Persistence of players is if the player's playerdata should be saved when they leave the server. "
		+ "Players' persistence is reset back to 'true' when they join the server.",
	"Passengers inherit the persistence of their vehicle, meaning a persistent zombie put on a "
		+ "non-persistent chicken will become non-persistent. This does not apply to players.",
	"By default, all entities are persistent."
})
@Example("prevent all entities from persisting")
@Example("force {_leaves} to persist")
@Example("""
	command /kickcheater <cheater: player>:
		permission: op
		trigger:
			prevent {_cheater} from persisting
			kick {_cheater}
	""")
@Since("2.11")
public class EffPersistent extends Effect {

	static {
		Skript.registerEffect(EffPersistent.class,
			"make %entities/blocks% [:not] persist[ent]",
			"force %entities/blocks% to [:not] persist",
			"prevent %entities/blocks% from persisting");
	}

	private Expression<?> source;
	private boolean persist;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		source = exprs[0];
		if (matchedPattern < 2) {
			persist = !parseResult.hasTag("not");
		} else {
			persist = false;
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Object object : source.getArray(event)) {
			if (object instanceof Entity entity) {
				entity.setPersistent(persist);
			} else if (object instanceof Block block && block.getBlockData() instanceof Leaves leaves) {
				leaves.setPersistent(persist);
				block.setBlockData(leaves);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (persist)
			return "make " + source.toString(event, debug) + " persistent";
		return "prevent " + source.toString(event, debug) + " from persisting";
	}

}
