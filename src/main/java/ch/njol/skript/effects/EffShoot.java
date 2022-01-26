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
package ch.njol.skript.effects;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Shoot")
@Description("Shoots a projectile (or any other entity) from a given entity.")
@Examples({"shoot an arrow",
		"make the player shoot a creeper at speed 10",
		"shoot a pig from the creeper"})
@Since("1.4")
public class EffShoot extends Effect {
	static {
		Skript.registerEffect(EffShoot.class,
				"shoot %entitydatas% [from %livingentities/locations%] [(at|with) (speed|velocity) %-number%] [%-direction%]",
				"(make|let) %livingentities/locations% shoot %entitydatas% [(at|with) (speed|velocity) %-number%] [%-direction%]");
	}
	
	private final static Double DEFAULT_SPEED = 5.;
	
	@SuppressWarnings("null")
	private Expression<EntityData<?>> types;
	@SuppressWarnings("null")
	private Expression<?> shooters;
	@Nullable
	private Expression<Number> velocity;
	@Nullable
	private Expression<Direction> direction;
	
	@Nullable
	public static Entity lastSpawned = null;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		types = (Expression<EntityData<?>>) exprs[matchedPattern];
		shooters = exprs[1 - matchedPattern];
		velocity = (Expression<Number>) exprs[2];
		direction = (Expression<Direction>) exprs[3];
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected void execute(final Event e) {
		lastSpawned = null;
		final Number v = velocity != null ? velocity.getSingle(e) : DEFAULT_SPEED;
		if (v == null)
			return;
		final Direction dir = direction != null ? direction.getSingle(e) : Direction.IDENTITY;
		if (dir == null)
			return;
		for (final Object shooter : shooters.getArray(e)) {
			for (final EntityData<?> d : types.getArray(e)) {
				if (shooter instanceof LivingEntity) {
					final Vector vel = dir.getDirection(((LivingEntity) shooter).getLocation()).multiply(v.doubleValue());
					final Class<? extends Entity> type = d.getType();
					if (Fireball.class.isAssignableFrom(type)) {// fireballs explode in the shooter's face by default
						final Fireball projectile = (Fireball) ((LivingEntity) shooter).getWorld().spawn(((LivingEntity) shooter).getEyeLocation().add(vel.clone().normalize().multiply(0.5)), type);
						projectile.setShooter((ProjectileSource) shooter);
						projectile.setVelocity(vel);
						lastSpawned = projectile;
					} else if (Projectile.class.isAssignableFrom(type)) {
						@SuppressWarnings("unchecked")
						final Projectile projectile = ((LivingEntity) shooter).launchProjectile((Class<? extends Projectile>) type);
						set(projectile, d);
						projectile.setVelocity(vel);
						lastSpawned = projectile;
					} else {
						final Location loc = ((LivingEntity) shooter).getLocation();
						loc.setY(loc.getY() + ((LivingEntity) shooter).getEyeHeight() / 2);
						final Entity projectile = d.spawn(loc);
						if (projectile != null)
							projectile.setVelocity(vel);
						lastSpawned = projectile;
					}
				} else {
					final Vector vel = dir.getDirection((Location) shooter).multiply(v.doubleValue());
					final Entity projectile = d.spawn((Location) shooter);
					if (projectile != null)
						projectile.setVelocity(vel);
					lastSpawned = projectile;
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <E extends Entity> void set(final Entity e, final EntityData<E> d) {
		d.set((E) e);
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "shoot " + types.toString(e, debug) + " from " + shooters.toString(e, debug) + (velocity != null ? " at speed " + velocity.toString(e, debug) : "") + (direction != null ? " " + direction.toString(e, debug) : "");
	}
	
}
