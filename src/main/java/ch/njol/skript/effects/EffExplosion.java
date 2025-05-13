package ch.njol.skript.effects;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Explosion")
@Description({"Creates an explosion of a given force. The Minecraft Wiki has an <a href='https://www.minecraft.wiki/w/Explosion'>article on explosions</a> " +
		"which lists the explosion forces of TNT, creepers, etc.",
		"Hint: use a force of 0 to create a fake explosion that does no damage whatsoever, or use the explosion effect introduced in Skript 2.0.",
		"Starting with Bukkit 1.4.5 and Skript 2.0 you can use safe explosions which will damage entities but won't destroy any blocks."})
@Examples({"create an explosion of force 10 at the player",
		"create an explosion of force 0 at the victim"})
@Since("1.0")
public class EffExplosion extends Effect {

	static {
		Skript.registerEffect(EffExplosion.class,
				"[(create|make)] [an] explosion (of|with) (force|strength|power) %number% [%directions% %locations%] [(1¦with fire)]",
				"[(create|make)] [a] safe explosion (of|with) (force|strength|power) %number% [%directions% %locations%]",
				"[(create|make)] [a] fake explosion [%directions% %locations%]",
				"[(create|make)] [an] explosion[ ]effect [%directions% %locations%]");
	}

	@Nullable
	private Expression<Number> force;
	@SuppressWarnings("null")
	private Expression<Location> locations;

	private boolean blockDamage;

	private boolean setFire;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		force = matchedPattern <= 1 ? (Expression<Number>) exprs[0] : null;
		blockDamage = matchedPattern != 1;
		setFire = parser.mark == 1;
		locations = Direction.combine((Expression<? extends Direction>) exprs[exprs.length - 2], (Expression<? extends Location>) exprs[exprs.length - 1]);
		return true;
	}

	@Override
	public void execute(final Event e) {
		final Number power = force != null ? force.getSingle(e) : 0;
		if (power == null)
			return;
		for (Location location : locations.getArray(e)) {
			if (location.getWorld() == null)
				continue;
			if (!blockDamage)
				location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), power.floatValue(), false, false);
			else
				location.getWorld().createExplosion(location, power.floatValue(), setFire);
		}
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		if (force != null)
			return "create explosion of force " + force.toString(e, debug) + " " + locations.toString(e, debug);
		else
			return "create explosion effect " + locations.toString(e, debug);
	}

}
