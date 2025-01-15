package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("Explosion")
@Description({
	"Creates an explosion of a given force. The Minecraft Wiki has an <a href='https://www.minecraft.wiki/w/Explosion'>article on explosions</a> " +
	"which lists the explosion forces of TNT, creepers, etc.",
	"Use a force of 0 to create a fake explosion that does no damage whatsoever, or use the 'fake explosion' effect.",
	"Use safe explosions to create an explosion which damages entities but won't destroy any blocks."})
@Examples({
	"create an explosion of force 10 at the player with fire",
	"create a safe explosion with force 10",
	"create a fake explosion at the player",
})
@Since("1.0")
public class EffExplosion extends Effect {

	static {
		Skript.registerEffect(EffExplosion.class,
				"[create|make] [an] explosion (of|with) (force|strength|power) %number% [%directions% %locations%] [fire:with fire]",
				"[create|make] [a] safe explosion (of|with) (force|strength|power) %number% [%directions% %locations%]",
				"[create|make] [a] fake explosion [%directions% %locations%]",
				"[create|make] [an] explosion[ ]effect [%directions% %locations%]");

		EventValues.registerEventValue(ScriptExplodeEvent.class, Location.class, ScriptExplodeEvent::location);
		EventValues.registerEventValue(ScriptExplodeEvent.class, Number.class, ScriptExplodeEvent::power);
	}

	private Expression<Number> force;
	private Expression<Location> locations;

	private boolean blockDamage;
	private boolean setFire;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		//noinspection unchecked
		force = matchedPattern <= 1 ? (Expression<Number>) exprs[0] : null;
		blockDamage = matchedPattern != 1;
		setFire = parser.hasTag("fire");
		//noinspection unchecked
		locations = Direction.combine((Expression<? extends Direction>) exprs[exprs.length - 2],
			(Expression<? extends Location>) exprs[exprs.length - 1]);
		return true;
	}

	@Override
	public void execute(Event event) {
		Number optionalForce = force != null ? force.getSingle(event) : 0;
		if (optionalForce == null)
			return;

		float power = optionalForce.floatValue();

		for (Location location : locations.getArray(event)) {
			if (location.getWorld() == null)
				continue;

			boolean cancelled;
			if (!blockDamage) {
				cancelled = location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(),
					power, false, false);
			} else {
				cancelled = location.getWorld().createExplosion(location, power, setFire);
			}

			if (!cancelled)
				Bukkit.getPluginManager().callEvent(new ScriptExplodeEvent(location, power));
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (force != null) {
			return "create explosion of force " + force.toString(event, debug) + " " + locations.toString(event, debug);
		} else {
			return "create explosion effect " + locations.toString(event, debug);
		}
	}

	/**
	 * Event for handling explosions created by this effect.
	 */
	public static class ScriptExplodeEvent extends Event {

		private static final HandlerList HANDLER_LIST = new HandlerList();

		private final Location at;
		private final float power;

		public ScriptExplodeEvent(@NotNull Location at, float power) {
			this.at = at;
			this.power = power;
		}

		public @NotNull Location location() {
			return at;
		}

		public float power() {
			return power;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			return HANDLER_LIST;
		}

		public static HandlerList getHandlerList() {
			return HANDLER_LIST;
		}

	}

}
