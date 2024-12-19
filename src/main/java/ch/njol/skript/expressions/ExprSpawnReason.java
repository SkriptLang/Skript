package ch.njol.skript.expressions;

import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;

@Name("Spawn Reason")
@Description("The <a href='classes.html#spawnreason'>spawn reason</a> in a <a href='events.html#spawn'>spawn</a> event.")
@Examples({
	"on spawn:",
		"\tspawn reason is reinforcements or breeding",
		"\tcancel event"
})
@Since("2.3")
public class ExprSpawnReason extends EventValueExpression<SpawnReason> {

	static {
		register(ExprSpawnReason.class, SpawnReason.class, "spawn[ing] reason");
	}

	public ExprSpawnReason() {
		super(SpawnReason.class);
	}

}
