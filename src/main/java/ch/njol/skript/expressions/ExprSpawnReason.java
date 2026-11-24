package ch.njol.skript.expressions;

import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;

@Name("Spawn Reason")
@Description("The [reason](docs:Skript/TypeSpawnreason) in a [spawn](docs:Skript/EvtSpawn) event.")
@Example("""
	on spawn:
		spawn reason is reinforcements or breeding
		cancel event
	""")
@Since("2.3")
public class ExprSpawnReason extends EventValueExpression<SpawnReason> {

	static {
		register(ExprSpawnReason.class, SpawnReason.class, "spawn[ing] reason");
	}

	public ExprSpawnReason() {
		super(SpawnReason.class);
	}

}
