package ch.njol.skript.expressions;

import java.lang.reflect.Array;

import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;

@Name("Attacked")
@Description("The victim of a damage event, e.g. when a player attacks a zombie this expression represents the zombie. " +
			 "When using Minecraft 1.11+, this also covers the hit entity in a projectile hit event.")
@Examples({"on damage:",
	"\tvictim is a creeper",
	"\tdamage the attacked by 1 heart"})
@Since("1.3, 2.6.1 (projectile hit event)")
@Events({"damage", "death", "projectile hit"})
public class ExprAttacked extends SimpleExpression<Entity> implements EventRestrictedSyntax {

	private static final boolean SUPPORT_PROJECTILE_HIT = Skript.methodExists(ProjectileHitEvent.class, "getHitEntity");

	static {
		Skript.registerExpression(ExprAttacked.class, Entity.class, ExpressionType.SIMPLE, "[the] (attacked|damaged|victim) [<(.+)>]");
	}

	@SuppressWarnings({"null", "NotNullFieldNotInitialized"})
	private EntityData<?> type;

	@Override
	public boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		String type = parser.regexes.size() == 0 ? null : parser.regexes.get(0).group();
		if (type == null) {
			this.type = EntityData.fromClass(Entity.class);
		} else {
			EntityData<?> t = EntityData.parse(type);
			if (t == null) {
				Skript.error("'" + type + "' is not an entity type", ErrorQuality.NOT_AN_EXPRESSION);
				return false;
			}
			this.type = t;
		}
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(EntityDamageEvent.class, EntityDeathEvent.class,
			VehicleDamageEvent.class, VehicleDestroyEvent.class, ProjectileHitEvent.class);
	}

	@Override
	@Nullable
	protected Entity[] get(Event e) {
		Entity[] one = (Entity[]) Array.newInstance(type.getType(), 1);
		Entity entity;
		if (e instanceof EntityEvent)
			if (SUPPORT_PROJECTILE_HIT && e instanceof ProjectileHitEvent)
				entity = ((ProjectileHitEvent) e).getHitEntity();
			else
				entity = ((EntityEvent) e).getEntity();
		else if (e instanceof VehicleEvent)
			entity = ((VehicleEvent) e).getVehicle();
		else
			return null;
		if (type.isInstance(entity)) {
			one[0] = entity;
			return one;
		}
		return null;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return type.getType();
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (e == null)
			return "the attacked " + type;
		return Classes.getDebugMessage(getSingle(e));
	}

}
