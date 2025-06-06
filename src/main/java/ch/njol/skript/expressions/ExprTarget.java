package ch.njol.skript.expressions;

import java.util.function.Predicate;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Target")
@Description({
	"For players this is the entity at the crosshair.",
	"For mobs and experience orbs this is the entity they are attacking/following (if any).",
	"Display entities have a hit box of 0, so you should use 'target display' to collect Display entities",
	"May grab entities in unloaded chunks."
})
@Examples({
	"on entity target:",
		"\tif entity's target is a player:",
			"\t\tsend \"You're being followed by an %entity%!\" to target of entity",
	"",
	"reset target of entity # Makes the entity target-less",
	"delete targeted entity of player # for players it will delete the target",
	"delete target of last spawned zombie # for entities it will make them target-less"
})
@Since("1.4.2, 2.7 (Reset), 2.8.0 (ignore blocks)")
public class ExprTarget extends PropertyExpression<LivingEntity, Entity> {

	static {
		Skript.registerExpression(ExprTarget.class, Entity.class, ExpressionType.PROPERTY,
				"[the] target[[ed] %-*entitydata%] [of %livingentities%] [blocks:ignoring blocks] [[with|at] ray[ ]size %-number%]", // TODO add a where filter when extendable https://github.com/SkriptLang/Skript/issues/4856
				"%livingentities%'[s] target[[ed] %-*entitydata%] [blocks:ignoring blocks] [[with|at] ray[ ]size %-number%]"
		);
	}

	private static boolean ignoreBlocks;
	private static int targetBlockDistance;

	@Nullable
	private Expression<Number> raysize;

	@Nullable
	private EntityData<?> type;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		type = exprs[matchedPattern] == null ? null : (EntityData<?>) exprs[matchedPattern].getSingle(null);
		setExpr((Expression<? extends LivingEntity>) exprs[1 - matchedPattern]);
		targetBlockDistance = SkriptConfig.maxTargetBlockDistance.value();
		if (targetBlockDistance < 0)
			targetBlockDistance = 100;
		ignoreBlocks = parser.hasTag("blocks");
		raysize = (Expression<Number>) exprs[2];
		return true;
	}

	@Override
	protected Entity[] get(Event event, LivingEntity[] source) {
		double raysize = this.raysize != null ? this.raysize.getOptionalSingle(event).orElse(0.0).doubleValue() : 0.0D;
		return get(source, entity -> {
			if (event instanceof EntityTargetEvent && entity.equals(((EntityTargetEvent) event).getEntity()) && !Delay.isDelayed(event)) {
				Entity target = ((EntityTargetEvent) event).getTarget();
				if (target == null || type != null && !type.isInstance(target))
					return null;
				return target;
			}
			return getTarget(entity, type, raysize);
		});
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case RESET:
			case DELETE:
				return CollectionUtils.array(LivingEntity.class);
			default:
				return super.acceptChange(mode);
		}
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET || mode == ChangeMode.DELETE) {
			LivingEntity target = delta == null ? null : (LivingEntity) delta[0]; // null will make the entity target-less (reset target) but for players it will remove them.
			if (event instanceof EntityTargetEvent) {
				EntityTargetEvent targetEvent = (EntityTargetEvent) event;
				for (LivingEntity entity : getExpr().getArray(event)) {
					if (entity.equals(targetEvent.getEntity()))
						targetEvent.setTarget(target);
				}
			} else {
				double raysize = this.raysize != null ? this.raysize.getOptionalSingle(event).orElse(0.0).doubleValue() : 0.0D;
				for (LivingEntity entity : getExpr().getArray(event)) {
					if (entity instanceof Mob) {
						((Mob) entity).setTarget(target);
					} else if (entity instanceof Player && mode == ChangeMode.DELETE) {
						Entity playerTarget = getTarget(entity, type, raysize);
						if (playerTarget != null && !(playerTarget instanceof OfflinePlayer))
							playerTarget.remove();
					}
				}
			}
			return;
		}
		super.change(event, delta, mode);
	}

	@Override
	public boolean setTime(int time) {
		if (time != EventValues.TIME_PAST)
			return super.setTime(time, EntityTargetEvent.class, getExpr());
		return super.setTime(time);
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return type != null ? type.getType() : Entity.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "target" + (type == null ? "" : "ed " + type) + (getExpr().isDefault() ? "" : " of " + getExpr().toString(event, debug));
	}

	/**
	 * Gets an entity's target entity.
	 *
	 * @param origin The entity to get the target of.
	 * @param type The exact EntityData to find. Can be null for any entity.
	 * @param raysize The size of the ray for the raytrace.
	 * @return The entity's target.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public static <T extends Entity> T getTarget(LivingEntity origin, @Nullable EntityData<T> type, double raysize) {
		if (origin instanceof Mob)
			return ((Mob) origin).getTarget() == null || type != null && !type.isInstance(((Mob) origin).getTarget()) ? null : (T) ((Mob) origin).getTarget();

		Predicate<Entity> predicate = entity -> {
			if (entity.equals(origin))
				return false;
			if (type != null && !type.isInstance(entity))
				return false;
			//noinspection RedundantIfStatement
			if (entity instanceof Player && ((Player) entity).getGameMode() == GameMode.SPECTATOR)
				return false;
			return true;
		};

		Location eyes = origin.getEyeLocation();
		Vector direction = origin.getLocation().getDirection();

		double distance = targetBlockDistance;
		if (!ignoreBlocks) {
			RayTraceResult blockResult = origin.getWorld().rayTraceBlocks(eyes, direction, targetBlockDistance);
			if (blockResult != null) {
				Vector hit = blockResult.getHitPosition();
				distance = eyes.toVector().distance(hit);
			}
		}

		RayTraceResult result = origin.getWorld().rayTraceEntities(eyes, direction, distance, raysize, predicate);
		if (result == null)
			return null;
		Entity hitEntity = result.getHitEntity();
		if (hitEntity == null)
			return null;
		return (T) result.getHitEntity();
	}

}
