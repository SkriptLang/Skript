package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Goat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Make Entity Scream")
@Description("Make a goat or enderman start or stop screaming.")
@Example("""
		make last spawned goat start screaming
		force last spawned goat to stop screaming
	"""
)
@Example("""
		make {_enderman} scream
		force {_enderman} to stop screaming
	"""
)
@RequiredPlugins("Paper (endermen)")
@Since("INSERT VERSION")
public class EffScreaming extends Effect {

	private static final boolean SUPPORTS_ENDERMAN = Skript.methodExists(Enderman.class, "setScreaming", boolean.class);

	static {
		Skript.registerEffect(EffScreaming.class,
			"make %livingentities% (start screaming|scream)",
			"force %livingentities% to (start screaming|scream)",
			"make %livingentities% stop screaming",
			"force %livingentities% to stop screaming");
	}

	private Expression<LivingEntity> entities;
	private boolean scream;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		scream = matchedPattern <= 1;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Goat goat) {
				goat.setScreaming(scream);
			} else if (SUPPORTS_ENDERMAN && entity instanceof Enderman enderman) {
				enderman.setScreaming(scream);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + entities.toString(event, debug) + (scream ? " start " : " stop ") + "screaming";
	}

}
