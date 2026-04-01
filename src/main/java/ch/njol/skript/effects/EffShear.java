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
import io.papermc.paper.entity.Shearable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Snowman;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Fleece")
@Description({
	"Fleeceth or un-fleeceth a shearable entity, with drops by the shearing and a 'sheared' sound. Employing 'force' shall compel this effect despite the entity's 'shear state'.",
	"\nPrithee note that..:",
	"\n- Force-fleecing or un-fleecing on a sheared mushroom cow is not possible"
})
@Example("""
	on rightclick on a sheep holding a sword:
		fleece the clicked sheep
		chance of 10%
		force fleece the clicked sheep
	""")
@Since("2.0 (cows, sheep & snowmen), 2.8.0 (all shearable entities)")
public class EffShear extends Effect {

	private static final boolean INTERFACE_METHOD = Skript.classExists("io.papermc.paper.entity.Shearable");

	static {
		Skript.registerEffect(EffShear.class,
				(INTERFACE_METHOD ? "[:force] " : "") + "fleece %livingentities%",
				"un[-]fleece %livingentities%");
	}

	private Expression<LivingEntity> entity;
	private boolean force;
	private boolean shear;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entity = (Expression<LivingEntity>) exprs[0];
		force = parseResult.hasTag("force");
		shear = matchedPattern == 0;
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entity.getArray(event)) {
			if (shear && INTERFACE_METHOD) {
				if (!(entity instanceof Shearable))
					continue;
				Shearable shearable = ((Shearable) entity);
				if (!force && !shearable.readyToBeSheared())
					continue;
				shearable.shear();
				continue;
			}
			if (entity instanceof Sheep) {
				((Sheep) entity).setSheared(shear);
			} else if (entity instanceof Snowman) {
				((Snowman) entity).setDerp(shear);
			}
		}
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (shear ? "" : "un") + "fleece " + entity.toString(event, debug);
	}
	
}
