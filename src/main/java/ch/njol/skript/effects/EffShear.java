package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.*;
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
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Shear")
@Description({
	"Shears or un-shears a shearable entity with drops by shearing and a 'sheared' sound. Using 'force' will force this effect despite the entity's 'shear state'.",
	"Please note that:",
	"- If your server is not running with Paper 1.19.4 or higher, this effect will only change its 'shear state', and the 'force' effect is unavailable",
	"- Force-shearing or un-shearing on a sheared mushroom cow is not possible"
})
@Examples({
	"on rightclick on a sheep holding a sword:",
		"\tshear the clicked sheep",
		"\tchance of 10%",
		"\tforce shear the clicked sheep"
})
@Since("2.0 (cows, sheep & snowmen), 2.8.0 (all shearable entities)")
@RequiredPlugins("Paper 1.19.4+ (all shearable entities)")
public class EffShear extends Effect implements SyntaxRuntimeErrorProducer {

	private static final boolean INTERFACE_METHOD = Skript.classExists("io.papermc.paper.entity.Shearable");

	static {
		Skript.registerEffect(EffShear.class,
			(INTERFACE_METHOD ? "[:force] " : "") + "shear %livingentities%",
			"un[-]shear %livingentities%");
	}

	private Node node;
	private Expression<LivingEntity> entity;
	private boolean force, shear;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		node = getParser().getNode();
		entity = (Expression<LivingEntity>) exprs[0];
		force = parseResult.hasTag("force");
		shear = matchedPattern == 0;
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entity.getArray(event)) {
			if (shear && INTERFACE_METHOD) {
				if (!(entity instanceof Shearable shearable)) {
					warning("Entity type " + entity.getType() + " is not shearable.", this.entity.toString(null, false));
					continue;
				}
				if (!force && !shearable.readyToBeSheared()) {
					warning("An entity couldn't be sheared as it wasn't ready, and the 'force' option wasn't specified.", "shear" + this.entity.toString(null, false));
					continue;
				}
				shearable.shear();
				continue;
			}
			if (entity instanceof Sheep sheep) {
				sheep.setSheared(shear);
			} else if (entity instanceof Snowman snowman) {
				snowman.setDerp(shear);
			}
		}
	}

	@Override
	public Node getNode() {
		return node;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (shear ? "" : "un") + "shear " + entity.toString(event, debug);
	}

}
