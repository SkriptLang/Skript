package ch.njol.skript.expressions;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Attacker")
@Description({"The attacker of a damage event, e.g. when a player attacks a zombie this expression represents the player.",
		"Please note that the attacker can also be a block, e.g. a cactus or lava, but this expression will not be set in these cases."})
@Examples({"on damage:",
		"	attacker is a player",
		"	health of attacker is less than or equal to 2",
		"	damage victim by 1 heart"})
@Since("1.3")
@Events({"damage", "death", "destroy"})
public class ExprAttacker extends SimpleExpression<Entity> {

	static {
		Skript.registerExpression(ExprAttacker.class, Entity.class, ExpressionType.SIMPLE, "[the] (attacker|damager)");
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		if (!getParser().isCurrentEvent(EntityDamageEvent.class, EntityDeathEvent.class, VehicleDamageEvent.class, VehicleDestroyEvent.class)) {
			Skript.error("Cannot use 'attacker' outside of a damage/death/destroy event", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}
	
	@Override
	protected Entity[] get(Event e) {
		return new Entity[] {getAttacker(e)};
	}
	
	@Nullable
	private static Entity getAttacker(@Nullable Event e) {
		if (e == null)
			return null;
		if (e instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) e;
			if (edbee.getDamager() instanceof Projectile) {
				Projectile p = (Projectile) edbee.getDamager();
				Object o = p.getShooter();
				if (o instanceof Entity)
					return (Entity) o;
				return null;
			}
			return edbee.getDamager();
//		} else if (e instanceof EntityDamageByBlockEvent) {
//			return ((EntityDamageByBlockEvent) e).getDamager();
		} else if (e instanceof EntityDeathEvent) {
			return getAttacker(((EntityDeathEvent) e).getEntity().getLastDamageCause());
		} else if (e instanceof VehicleDamageEvent) {
			return ((VehicleDamageEvent) e).getAttacker();
		} else if (e instanceof VehicleDestroyEvent) {
			return ((VehicleDestroyEvent) e).getAttacker();
		}
		return null;
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (e == null)
			return "the attacker";
		return Classes.getDebugMessage(getSingle(e));
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
}
