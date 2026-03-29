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

@Name("Enduring Persistence")
@Description({
	"Render entities, players, or leaves to be persistent in their endurance.",
	"The persistence of entities doth determine whether they be retained through server restarts.",
	"The persistence of leaves doth determine whether they should decay when not bound to a log block within six meters.",
	"The persistence of players doth determine whether their playerdata be preserved when they depart the server."
		+ "A player's persistence is restored to 'true' upon their return to the server.",
	"Passengers do inherit the persistence of their conveyance, meaning a persistent zombie set upon a"
		+ "non-persistent chicken shall become non-persistent. This doth not apply to players.",
	"By default, all entities are persistent."
})
@Example("forbid all entities from persisting")
@Example("compel {_leaves} to persist")
@Example("""
    command /kickcheater <cheater: player>:
    	permission: op
    	trigger:
    		forbid {_cheater} from persisting
    		kick {_cheater}
    """)
@Since("2.11")
public class EffPersistent extends Effect {

	static {
		Skript.registerEffect(EffPersistent.class,
			"render %entities/blocks% [:not] persist[ent]",
			"compel %entities/blocks% to [:not] persist",
			"forbid %entities/blocks% from persisting");
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
