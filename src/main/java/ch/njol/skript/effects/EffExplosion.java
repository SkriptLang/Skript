package ch.njol.skript.effects;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
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
@Name("Conjure an Explosion")
@Description({"Conjure an explosion of a given force. The Minecraft Wiki holdeth an <a href='https://www.minecraft.wiki/w/Explosion'>article upon explosions</a> " +
		"which doth enumerate the explosive forces of TNT, creepers, and their ilk.",
		"Prithee note: employ a force of 0 to conjure a false explosion that causeth no harm whatsoever, or employ the explosion spectacle introduced in Skript 2.0.",
		"Since Bukkit 1.4.5 and Skript 2.0, one may conjure safe explosions which shall wound entities yet destroy no blocks."})
@Example("conjure an explosion of force 10 at the player")
@Example("conjure an explosion of force 0 at the victim")
@Since("1.0")
public class EffExplosion extends Effect {

	static {
		Skript.registerEffect(EffExplosion.class,
				"[(conjure|create)] [an] explosion (of|with) (force|strength|power) %number% [%directions% %locations%] [(1¦with fire)]",
				"[(conjure|create)] [a] safe explosion (of|with) (force|strength|power) %number% [%directions% %locations%]",
				"[(conjure|create)] [a] false explosion [%directions% %locations%]",
				"[(conjure|create)] [an] explosion[ ]spectacle [%directions% %locations%]");
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
