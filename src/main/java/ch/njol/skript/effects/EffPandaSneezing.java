package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Panda;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Make Panda Sneeze")
@Description("Make a panda start/stop sneezing.")
@Example("""
	if last spawned panda is not sneezing:
		make last spawned panda start sneezing
	""")
@Since("2.11")
public class EffPandaSneezing extends Effect {

	static {
		Skript.registerEffect(EffPandaSneezing.class,
			"make %livingentities% (start:(start sneezing|sneeze)|stop sneezing)",
			"force %livingentities% to (:start|stop) sneezing");
	}

	private Expression<LivingEntity> entities;
	private boolean start;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		start = parseResult.hasTag("start");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Panda panda) {
				panda.setSneezing(start);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("make", entities);
		if (start) {
			builder.append("start");
		} else {
			builder.append("stop");
		}
		builder.append("sneezing");
		return builder.toString();
	}

}
