/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
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
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Name("Shoot")
@Description({"Shoots a projectile (or any other entity) from a given entity.",
	"This can be used as an effect and as a section.",
	"If it is used as a section, the section is run before the entity is added to the world.",
	"You can modify the entity in this section, using for example 'event-entity' or 'event-arrow'.",
	"Do note that other event values, such as 'player', won't work in this section."})
@Examples({"shoot an arrow",
	"make the player shoot a creeper at speed 10",
	"shoot a pig from the creeper",
	"",
	"# Shoot section example",
	"command /firearrow:",
	"\ttrigger:",
	"\t\tshoot an arrow:",
	"\t\t\tignite event-projectile"})
@Since("1.4, INSERT VERSION (with section)")
public class EffSecShoot extends EffectSection {

	public static class ShootEvent extends Event {

		private final Entity entity;

		public ShootEvent(Entity entity) {
			this.entity = entity;
		}

		public Entity getEntity() {
			return entity;
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
			"(make|let) %livingentities/locations% shoot %entitydatas% [(at|with) (speed|velocity) %-number%] [%-direction%]");
		EventValues.registerEventValue(ShootEvent.class, Entity.class, new Getter<Entity, ShootEvent>() {
			@Override
			public Entity get(ShootEvent shootEvent) {
				return shootEvent.getEntity();
			}
		}, 0);
	}

	private static final Double DEFAULT_SPEED = 5.0;

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<EntityData<?>> types;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> shooters;
	@Nullable
	private Expression<Number> velocity;
	@Nullable
	private Expression<Direction> direction;

	@Nullable
	public static Entity lastSpawned = null;

	@Nullable
	private Trigger trigger;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs,
						int matchedPattern,
						Kleenean isDelayed,
						ParseResult parseResult,
						@Nullable SectionNode sectionNode,
						@Nullable List<TriggerItem> triggerItems) {
		types = (Expression<EntityData<?>>) exprs[matchedPattern];
		shooters = exprs[1 - matchedPattern];
		velocity = (Expression<Number>) exprs[2];
		direction = (Expression<Direction>) exprs[3];

		if (sectionNode != null) {
			trigger = loadCode(sectionNode, "shoot", ShootEvent.class);
		}

		return true;
	}

	@Override
	@Nullable
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected TriggerItem walk(Event e) {
		lastSpawned = null;

		Object localVars = Variables.copyLocalVariables(e);

		Consumer<? extends Entity> consumer;
		if (trigger != null) {
			consumer = o -> {
				lastSpawned = o;
				ShootEvent shootEvent = new ShootEvent(o);
				// Copy the local variables from the calling code to this section
				Variables.setLocalVariables(shootEvent, localVars);
				trigger.execute(shootEvent);
			};
		} else {
			consumer = null;
		}

		Number v = velocity != null ? velocity.getSingle(e) : DEFAULT_SPEED;
		if (v != null) {
			Direction dir = direction != null ? direction.getSingle(e) : Direction.IDENTITY;
			if (dir != null) {
				for (Object shooter : shooters.getArray(e)) {
					for (EntityData<?> d : types.getArray(e)) {
						if (shooter instanceof LivingEntity) {
							Vector vel = dir.getDirection(((LivingEntity) shooter).getLocation()).multiply(v.doubleValue());
							Class<? extends Entity> type = d.getType();
							if (Fireball.class.isAssignableFrom(type)) {// fireballs explode in the shooter's face by default
								Fireball projectile = (Fireball) d.spawn(((LivingEntity) shooter).getEyeLocation().add(vel.clone().normalize().multiply(0.5)), (Consumer) consumer);
								projectile.setShooter((ProjectileSource) shooter);
								projectile.setVelocity(vel);
								lastSpawned = projectile;
							} else if (Projectile.class.isAssignableFrom(type)) {
								@SuppressWarnings("unchecked")
								Projectile projectile = (Projectile) d.spawn(((LivingEntity) shooter).getEyeLocation(), (Consumer) consumer);
								set(projectile, d);
								projectile.setShooter((ProjectileSource) shooter);
								projectile.setVelocity(vel);
								lastSpawned = projectile;
							} else {
								Location loc = ((LivingEntity) shooter).getLocation();
								loc.setY(loc.getY() + ((LivingEntity) shooter).getEyeHeight() / 2);
								Entity projectile = d.spawn(loc, (Consumer) consumer);
								if (projectile != null)
									projectile.setVelocity(vel);
								lastSpawned = projectile;
							}
						} else {
							Vector vel = dir.getDirection((Location) shooter).multiply(v.doubleValue());
							Entity projectile = d.spawn((Location) shooter, (Consumer) consumer);
							if (projectile != null)
								projectile.setVelocity(vel);
							lastSpawned = projectile;
						}
					}
				}
			}
		}

		return super.walk(e, false);
	}

	@SuppressWarnings("unchecked")
	private static <E extends Entity> void set(Entity e, EntityData<E> d) {
		d.set((E) e);
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "shoot " + types.toString(e, debug) + " from " + shooters.toString(e, debug) + (velocity != null ? " at speed " +
				velocity.toString(e, debug) : "") + (direction != null ? " " + direction.toString(e, debug) : "");
	}

}
