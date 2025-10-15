package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Panda;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Force Eating")
@Description("Make a panda or horse type (horse, camel, donkey, llama, mule) start/stop eating.")
@Example("""
	if last spawned panda is eating:
		make last spawned panda stop eating
	""")
@Since("2.11")
public class EffEating extends Effect {

	private static final boolean SUPPORTS_HORSES = Skript.methodExists(AbstractHorse.class, "isEating");

	static {
		Skript.registerEffect(EffEating.class,
			"make %livingentities% (:start|stop) eating",
			"force %livingentities% to (:start|stop) eating");
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
				panda.setEating(start);
			} else if (SUPPORTS_HORSES && entity instanceof AbstractHorse horse) {
				horse.setEating(start);
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
		builder.append("eating");
		return builder.toString();
	}

}
