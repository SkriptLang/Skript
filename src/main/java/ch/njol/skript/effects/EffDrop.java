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

import ch.njol.skript.sections.EffSecSpawn;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.Experience;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Drop")
@Description("Drops one or more items.")
@Examples({"on death of creeper:",
		"	drop 1 TNT"})
@Since("1.0")
public class EffDrop extends Effect {
	static {
		Skript.registerEffect(EffDrop.class, "drop %itemtypes/experiences% [%directions% %locations%]",
				"drop %itemtypes/experiences% [%directions% %locations%] without velocity");
	}
	
	@Nullable
	public static Entity lastSpawned = null;
	
	@SuppressWarnings("null")
	private Expression<?> drops;
	@SuppressWarnings("null")
	private Expression<Location> locations;
	
	private boolean useVelocity = true;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		drops = exprs[0];
		locations = Direction.combine((Expression<? extends Direction>) exprs[1], (Expression<? extends Location>) exprs[2]);
		if (matchedPattern == 1) useVelocity = false;
		return true;
	}
	
	@Override
	public void execute(final Event e) {
		final Object[] os = drops.getArray(e);
		if (e instanceof EntityDeathEvent && locations.isSingle() && ((EntityDeathEvent) e).getEntity().getLocation().equals(locations.getSingle(e)) && !Delay.isDelayed(e)) {
			for (final Object o : os) {
				if (o instanceof Experience) {
					((EntityDeathEvent) e).setDroppedExp(((EntityDeathEvent) e).getDroppedExp() + ((Experience) o).getXP());
				} else {
					((ItemType) o).addTo(((EntityDeathEvent) e).getDrops());
				}
			}
			return;
		}
		for (final Location l : locations.getArray(e)) {
			final Location itemDropLoc = l.clone().subtract(0.5, 0.5, 0.5); // dropItemNaturally adds 0.15 to 0.85 randomly to all coordinates
			for (Object o : os) {
				if (o instanceof Experience) {
					final ExperienceOrb orb = l.getWorld().spawn(l, ExperienceOrb.class);
					orb.setExperience(((Experience) o).getXP());
					EffSecSpawn.lastSpawned = orb;
				} else {
					if (o instanceof ItemStack)
						o = new ItemType((ItemStack) o);
					for (final ItemStack is : ((ItemType) o).getItem().getAll()) {
						if (is.getType() != Material.AIR) {
							if (useVelocity) {
								lastSpawned = l.getWorld().dropItemNaturally(itemDropLoc, is);
							} else {
								final Item item = l.getWorld().dropItem(l, is);
								item.teleport(l);
								item.setVelocity(new Vector(0, 0, 0));
								lastSpawned = item;
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "drop " + drops.toString(e, debug) + " " + locations.toString(e, debug);
	}
	
}
