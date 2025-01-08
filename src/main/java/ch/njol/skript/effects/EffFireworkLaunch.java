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
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Launch firework")
@Description("Launch firework effects at the given location(s).")
@Examples("launch ball large colored red, purple and white fading to light green and black at player's location with duration 1")
@Since("2.4")
public class EffFireworkLaunch extends Effect implements SyntaxRuntimeErrorProducer {
	
	static {
		Skript.registerEffect(EffFireworkLaunch.class,
			"(launch|deploy) [[a] firework [with effect[s]]] %fireworkeffects% at %locations% [([with] (duration|power)|timed) %number%]");
	}

	public static @Nullable Entity lastSpawned = null;

	private Node node;
	private Expression<FireworkEffect> effects;
	private Expression<Location> locations;
	private Expression<Number> lifetime;
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		node = getParser().getNode();
		effects = (Expression<FireworkEffect>) exprs[0];
		locations = (Expression<Location>) exprs[1];
		lifetime = (Expression<Number>) exprs[2];
		return true;
	}

	@Override
	protected void execute(Event event) {
		FireworkEffect[] effects = this.effects.getArray(event);
		int power = 1;
		if (lifetime != null) {
			Number lifetime = this.lifetime.getSingle(event);
			if (lifetime == null) {
				warning("The duration of the firework was null, so defaulted to 1.", this.lifetime.toString(null, false));
			} else {
				power = lifetime.intValue();
			}
		}
		power = Math2.fit(0, power, 127);

		for (Location location : locations.getArray(event)) {
			World world = location.getWorld();
			if (world == null) {
				warning("One of the locations didn't have a world, and was skipped.", locations.toString(null, false));
				continue;
			}
			Firework firework = world.spawn(location, Firework.class);
			FireworkMeta meta = firework.getFireworkMeta();
			meta.addEffects(effects);
			meta.setPower(power);
			firework.setFireworkMeta(meta);
			lastSpawned = firework;
		}
	}

	@Override
	public Node getNode() {
		return node;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("launch firework(s)", effects, "at location(s)", locations, "timed", lifetime)
			.toString();
	}

}
