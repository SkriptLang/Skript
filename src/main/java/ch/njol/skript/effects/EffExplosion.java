package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Explosion")
@Description({
	"Creates an explosion of a given force. The Minecraft Wiki has an <a href='https://www.minecraft.wiki/w/Explosion'>article on explosions</a> " +
	"which lists the explosion forces of TNT, creepers, etc.",
	"Hint: use a force of 0 to create a fake explosion that does no damage whatsoever, or use the explosion effect introduced in Skript 2.0.",
	"Starting with Bukkit 1.4.5 and Skript 2.0 you can use safe explosions which will damage entities but won't destroy any blocks."
})
@Examples({
	"create an explosion of force 10 at the player",
	"create an explosion of force 0 at the victim"
})
@Since("1.0")
public class EffExplosion extends Effect implements SyntaxRuntimeErrorProducer {

	static {
		Skript.registerEffect(EffExplosion.class,
			"[(create|make)] [an] explosion (of|with) (force|strength|power) %number% [%directions% %locations%] [(1¦with fire)]",
			"[(create|make)] [a] safe explosion (of|with) (force|strength|power) %number% [%directions% %locations%]",
			"[(create|make)] [a] fake explosion [%directions% %locations%]",
			"[(create|make)] [an] explosion[ ]effect [%directions% %locations%]");
	}

	private Node node;
	private @Nullable Expression<Number> force;
	private Expression<Location> locations;
	private boolean blockDamage, setFire;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		node = getParser().getNode();
		force = matchedPattern <= 1 ? (Expression<Number>) exprs[0] : null;
		blockDamage = matchedPattern != 1;
		setFire = parser.mark == 1;
		locations = Direction.combine((Expression<? extends Direction>) exprs[exprs.length - 2], (Expression<? extends Location>) exprs[exprs.length - 1]);
		return true;
	}

	@Override
	public void execute(Event event) {
		float power = 0;
		if (force != null) {
			Number force = this.force.getSingle(event);
			if (force == null) {
				error("The provided force was not set.", this.force.toString());
				return;
			}
			power = force.floatValue();
		}
		for (Location location : locations.getArray(event)) {
			if (location.getWorld() == null) {
				warning("One of the locations didn't have a world, and was skipped.", locations.toString());
				continue;
			}
			if (!blockDamage)
				location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), power, false, false);
			else
				location.getWorld().createExplosion(location, power, setFire);
		}
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (force != null)
			return "create explosion of force " + force.toString(event, debug) + " " + locations.toString(event, debug);
		return "create explosion effect " + locations.toString(event, debug);
	}

}
