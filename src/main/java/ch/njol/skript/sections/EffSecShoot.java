package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.*;
import ch.njol.skript.util.Direction;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class EffSecShoot extends EffectSection {

	public static class ShootEvent extends Event {
		private Entity projectile, shooter;
		public ShootEvent(Entity projectile, @Nullable Entity shooter) {
			this.projectile = projectile;
			this.shooter = shooter;
		}

		public Entity getProjectile() {
			return projectile;
		}
		public Entity getShooter() {
			return shooter;
		}

		@Override
		@NotNull
		public HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	static {
		Skript.registerSection(EffSecShoot.class,
			"shoot %entitydatas% [from %livingentities/locations%] [(at|with) (speed|velocity) %-number%] [%-direction%]",
			"(make|let) %livingentities/locations% shoot %entitydatas% [(at|with) (speed|velocity) %-number%] [%-direction%]"
		);
	}

	private final static Double DEFAULT_SPEED = 5.;
	private Expression<EntityData<?>> types;
	private Expression<?> shooters;
	private @Nullable Expression<Number> velocity;
	private @Nullable Expression<Direction> direction;
	public static Entity lastSpawned = null;
	private @Nullable Trigger trigger;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult, @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> triggerItems) {
		types = (Expression<EntityData<?>>) exprs[matchedPattern];
		shooters = exprs[1 - matchedPattern];
		velocity = (Expression<Number>) exprs[2];
		direction = (Expression<Direction>) exprs[3];

		if (sectionNode != null) {
			AtomicBoolean delayed = new AtomicBoolean(false);
			Runnable afterLoading = () -> delayed.set(!getParser().getHasDelayBefore().isFalse());
			trigger = loadCode(sectionNode, "shoot", afterLoading, ShootEvent.class);
			if (delayed.get()) {
				Skript.error("Delays can't be used within a Shoot Effect Section");
				return false;
			}
		}
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected @Nullable TriggerItem walk(Event event) {
		lastSpawned = null;
		Number finalVelocity = velocity != null ? velocity.getSingle(event) : DEFAULT_SPEED;
		Direction finalDirection = direction != null ? direction.getSingle(event) : Direction.IDENTITY;
		if (finalVelocity == null || finalDirection == null)
			return null;

		Consumer<ShootEvent> consumer = null;
		if (trigger != null) {
			consumer = shootEvent -> {
				lastSpawned = shootEvent.getProjectile();
				Variables.setLocalVariables(shootEvent, Variables.copyLocalVariables(event));
				TriggerItem.walk(trigger, shootEvent);
				Variables.setLocalVariables(event, Variables.copyLocalVariables(event));
				Variables.removeLocals(shootEvent);
			};
		}

		for (Object shooter : shooters.getArray(event)) {
			for (EntityData<?> entityData : types.getArray(event)) {
				Entity finalProjectile = null;
				Vector vector;
				if (shooter instanceof LivingEntity livingShooter) {
					vector = finalDirection.getDirection(livingShooter.getLocation()).multiply(finalVelocity.doubleValue());
					Class<? extends Entity> type = entityData.getType();
					if (Fireball.class.isAssignableFrom(type)) {
						Fireball fireball = (Fireball) livingShooter.getWorld().spawn(livingShooter.getEyeLocation().add(vector.clone().normalize().multiply(0.5)), type);
						fireball.setShooter((ProjectileSource) shooter);
						finalProjectile = fireball;
					} else if (Projectile.class.isAssignableFrom(type)) {
						Projectile projectile =  livingShooter.launchProjectile((Class<? extends Projectile>) type);
						set(projectile, entityData);
						finalProjectile = projectile;
					} else {
						Location location = livingShooter.getLocation();
						location.setY(location.getY() + livingShooter.getEyeHeight() / 2);
						Entity projectile = entityData.spawn(location);
						finalProjectile = projectile;
					}
				} else {
					vector = finalDirection.getDirection((Location) shooter).multiply(finalVelocity.doubleValue());
					Entity projectile = entityData.spawn((Location) shooter);
					finalProjectile = projectile;
				}
				if (finalProjectile != null) {
					finalProjectile.setVelocity(vector);
					if (consumer != null) {
						consumer.accept(new ShootEvent(finalProjectile, shooter instanceof LivingEntity livingEntity ? livingEntity : null));
					} else {
						lastSpawned = finalProjectile;
					}
				}
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private static <E extends Entity> void set(final Entity e, final EntityData<E> d) {
		d.set((E) e);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "shoot " + types.toString(event, debug) + " from " + shooters.toString(event, debug) + (velocity != null ? " at speed " + velocity.toString(event, debug) : "") + (direction != null ? " " + direction.toString(event, debug) : "");
	}

}
