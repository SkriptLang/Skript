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
package ch.njol.skript.events;

import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("unchecked")
public class EvtDamage extends SkriptEvent {

	static {
		Skript.registerEvent("Damage", EvtDamage.class, EntityDamageEvent.class, "damag(e|ing) [of %entitydata%] [by %entitydata%]")
				.description("Called when an entity receives damage, e.g. by an attack from another entity, lava, fire, drowning, fall, suffocation, etc.")
				.examples("on damage:", "on damage of a player:", "on damage of player by zombie:")
				.since("1.0, INSERT VERSION (by entity)");
	}
	
	@Nullable
	private Literal<EntityData<?>> ofTypes, byTypes;
	
	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
		ofTypes = (Literal<EntityData<?>>) args[0];
		byTypes = (Literal<EntityData<?>>) args[1];
		return true;
	}
	
	@Override
	public boolean check(Event evt) {
		EntityDamageEvent e = (EntityDamageEvent) evt;
		if (evt instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) evt;
			if (!checkDamager(event.getDamager()))
				return false;
		} else if (byTypes != null) {
			return false;
		}
		if (!checkDamaged(e.getEntity()))
			return false;
		if (e instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) e).getDamager() instanceof EnderDragon && ((EntityDamageByEntityEvent) e).getEntity() instanceof EnderDragon)
			return false;
		return checkDamage(e);
	}

	private boolean checkDamager(Entity e) {
		if (byTypes != null) {
			for (EntityData<?> d : byTypes.getAll()) {
				if (d.isInstance(e))
					return true;
			}
			return false;
		}
		return true;
	}
	
	private boolean checkDamaged(Entity e) {
		if (ofTypes != null) {
			for (EntityData<?> d : ofTypes.getAll()) {
				if (d.isInstance(e))
					return true;
			}
			return false;
		}
		return true;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "damage" + (ofTypes != null ? " of " + ofTypes.toString(e, debug) : "") +
			(byTypes != null ? " by " + byTypes.toString(e, debug) : "");
	}
	
//	private final static WeakHashMap<LivingEntity, Integer> lastDamages = new WeakHashMap<LivingEntity, Integer>();
	
	private static boolean checkDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof LivingEntity))
			return true;
		LivingEntity en = (LivingEntity) e.getEntity();
		if (HealthUtils.getHealth(en) <= 0)
			return false;
//		if (en.getNoDamageTicks() <= en.getMaximumNoDamageTicks() / 2) {
//			lastDamages.put(en, e.getDamage());
//			return true;
//		}
//		final Integer lastDamage = lastDamages.get(en);
//		if (lastDamage != null && lastDamage >= e.getDamage())
//			return false;
//		lastDamages.put(en, e.getDamage());
		return true;
	}
	
	/*
	static {
		Bukkit.getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onDamage(final EntityDamageEvent e) {
				if (e.getEntity() == EffSecSpawn.lastSpawned) {
					final Entity en = e.getEntity();
					Skript.info("");
					Skript.info("- damage event! time: " + en.getWorld().getTime());
	//					Skript.info("entity: " + en);
					Skript.info("damage: " + e.getDamage());
	//					Skript.info("last damage: " + (en.getLastDamageCause() == null ? "<none>" : ""+en.getLastDamageCause().getDamage()));
					if (en instanceof LivingEntity) {
						Skript.info("is invincible: " + (((LivingEntity) en).getNoDamageTicks() > ((LivingEntity) en).getMaximumNoDamageTicks() / 2f));
						if (((LivingEntity) en).getNoDamageTicks() > 0)
							Skript.info("damage difference (positive = more): " + (e.getDamage() - en.getLastDamageCause().getDamage()));
						final int h = Math.max(((LivingEntity) en).getHealth(), 0);
						Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
							@Override
							public void run() {
								Skript.info("actual damage: " + (h - Math.max(((LivingEntity) en).getHealth(), 0)));
							}
						});
					}
					if (e instanceof EntityDamageByEntityEvent) {
						Skript.info("attacker: " + ((EntityDamageByEntityEvent) e).getDamager());
					}
				}
			}
		}, Skript.getInstance());
	}
	//	*/
}
