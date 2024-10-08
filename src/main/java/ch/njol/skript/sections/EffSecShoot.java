package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.*;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.Getter;
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

		private Entity projectile;
		private @Nullable LivingEntity shooter;

		public ShootEvent(Entity projectile, @Nullable LivingEntity shooter) {
			this.projectile = projectile;
			this.shooter = shooter;
		}

		public Entity getProjectile() {
			return projectile;
		}

		public @Nullable LivingEntity getShooter() {
			return shooter;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	static {
		Skript.registerSection(EffSecShoot.class,
			"shoot %entitydatas% [from %livingentities/locations%] [(at|with) (speed|velocity) %-number%] [%-direction%]",
			"(make|let) %livingentities/locations% shoot %entitydatas% [(at|with) (speed|velocity) %-number%] [%-direction%]"
		);
		EventValues.registerEventValue(ShootEvent.class, Entity.class, new Getter<Entity, ShootEvent>() {
			@Override
			public @Nullable Entity get(ShootEvent shootEvent) {
				return shootEvent.getProjectile();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(ShootEvent.class, Projectile.class, new Getter<Projectile, ShootEvent>() {
			@Override
			public @Nullable Projectile get(ShootEvent shootEvent) {
				return shootEvent.getProjectile() instanceof Projectile projectile ? projectile : null;
			}
		}, EventValues.TIME_NOW);
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
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> triggerItems) {
		types = (Expression<EntityData<?>>) exprs[matchedPattern];
		shooters = exprs[1 - matchedPattern];
		velocity = (Expression<Number>) exprs[2];
		direction = (Expression<Direction>) exprs[3];

		if (sectionNode != null) {
			AtomicBoolean delayed = new AtomicBoolean(false);
			Runnable afterLoading = () -> delayed.set(!getParser().getHasDelayBefore().isFalse());
			trigger = loadCode(sectionNode, "shoot", afterLoading, ShootEvent.class);
			if (delayed.get()) {
				Skript.error("Delays cannot be used within a 'shoot' effect section");
				return false;
			}
		}
		return true;
	}

	@Override
	@SuppressWarnings({"unchecked","rawtypes"})
	protected @Nullable TriggerItem walk(Event event) {
		lastSpawned = null;
		Number finalVelocity = velocity != null ? velocity.getSingle(event) : DEFAULT_SPEED;
		Direction finalDirection = direction != null ? direction.getSingle(event) : Direction.IDENTITY;
		if (finalVelocity == null || finalDirection == null)
			return null;

		for (Object shooter : shooters.getArray(event)) {
			for (EntityData<?> entityData : types.getArray(event)) {
				Entity finalProjectile = null;
				Vector vector;
				if (shooter instanceof LivingEntity livingShooter) {
					vector = finalDirection.getDirection(livingShooter.getLocation()).multiply(finalVelocity.doubleValue());
					Class<? extends Entity> type = entityData.getType();
					if (Fireball.class.isAssignableFrom(type)) {
						if (trigger != null) {
							livingShooter.getWorld().spawn(
								livingShooter.getEyeLocation().add(vector.clone().normalize().multiply(0.5)),
								type,
								(Consumer) getSpawnConsumer(event, vector, entityData, livingShooter)
							);
						} else {
							Fireball fireball = (Fireball) livingShooter.getWorld().spawn(
								livingShooter.getEyeLocation().add(vector.clone().normalize().multiply(0.5)),
								type
							);
							fireball.setShooter(livingShooter);
							finalProjectile = fireball;
						}
					} else if (Projectile.class.isAssignableFrom(type)) {
						if (trigger != null) {
							livingShooter.launchProjectile(
								(Class<? extends Projectile>) type,
								vector,
								(Consumer) getSpawnConsumer(event, vector, entityData, livingShooter)
							);
						} else {
							finalProjectile = livingShooter.launchProjectile((Class<? extends Projectile>) type);
							set(finalProjectile, entityData);
						}
					} else {
						Location location = livingShooter.getLocation();
						location.setY(location.getY() + livingShooter.getEyeHeight() / 2);
						if (trigger != null) {
							entityData.spawn(location, (Consumer) getSpawnConsumer(event, vector, entityData, livingShooter));
						} else {
							finalProjectile = entityData.spawn(location);
						}
					}
				} else {
					vector = finalDirection.getDirection((Location) shooter).multiply(finalVelocity.doubleValue());
					if (trigger != null) {
						entityData.spawn((Location) shooter, (Consumer) getSpawnConsumer(event, vector, entityData, null));
					} else {
						finalProjectile = entityData.spawn((Location) shooter);
					}
				}
				if (finalProjectile != null) {
					finalProjectile.setVelocity(vector);
					lastSpawned = finalProjectile;
				}
			}
		}

		return super.walk(event, false);
	}

	@SuppressWarnings("unchecked")
	private static <E extends Entity> void set(Entity entity, EntityData<E> entityData) {
		entityData.set((E) entity);
	}

	private Consumer<? extends Entity> getSpawnConsumer(Event event, Vector vector, EntityData<?> entityData, @Nullable LivingEntity shooter) {
		return entity -> {
			if (entity instanceof Fireball fireball)
				fireball.setShooter(shooter);
			else if (entity instanceof Projectile projectile && shooter != null) {
				projectile.setShooter(shooter);
				set(projectile, entityData);
			}
			entity.setVelocity(vector);
			ShootEvent shootEvent = new ShootEvent(entity, shooter);
			lastSpawned = entity;
			Variables.setLocalVariables(shootEvent, Variables.copyLocalVariables(event));
			TriggerItem.walk(trigger, shootEvent);
			Variables.setLocalVariables(event, Variables.copyLocalVariables(shootEvent));
			Variables.removeLocals(shootEvent);
		};
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "shoot " + types.toString(event, debug) + " from " + shooters.toString(event, debug) +
			(velocity != null ? " at speed " + velocity.toString(event, debug) : "") +
			(direction != null ? " " + direction.toString(event, debug) : "");
	}

}
