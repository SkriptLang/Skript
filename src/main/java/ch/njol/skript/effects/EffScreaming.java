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

@Name("Bid Entity Wail")
@Description("Bid a goat or enderman commence or cease its wailing.")
@Example("""
    make last spawned goat commence wailing
    compel last spawned goat to cease wailing
    	
    """
)
@Example("""
    make {_enderman} wail
    compel {_enderman} to cease wailing
    	
    """
)
@Since("2.11")
public class EffScreaming extends Effect {

	private static final boolean SUPPORTS_ENDERMAN = Skript.methodExists(Enderman.class, "setScreaming", boolean.class);

	static {
		Skript.registerEffect(EffScreaming.class,
			"make %livingentities% (commence wailing|wail)",
			"compel %livingentities% to (commence wailing|wail)",
			"make %livingentities% cease wailing",
			"compel %livingentities% to cease wailing");
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
