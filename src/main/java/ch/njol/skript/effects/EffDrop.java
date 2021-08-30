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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

@Name("Drop")
@Description("Drops one or more items.")
@Examples({"on death of creeper:",
		"	drop 1 TNT"})
@Since("1.0")
public class EffDrop extends Effect {

	static {
		Skript.registerEffect(EffDrop.class, "drop %itemtypes/experiences% [%directions% %locations%] [(1¦without velocity)]");
	}

	@Nullable
	public static Entity lastSpawned = null;

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> drops;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Location> locations;

	private boolean useVelocity;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		drops = exprs[0];
		locations = Direction.combine((Expression<? extends Direction>) exprs[1], (Expression<? extends Location>) exprs[2]);
		useVelocity = parseResult.mark == 0;
		return true;
	}

	@Override
	public void execute(Event e) {
		Object[] os = drops.getArray(e);
		for (Location l : locations.getArray(e)) {
			Location itemDropLoc = l.clone().subtract(0.5, 0.5, 0.5); // dropItemNaturally adds 0.15 to 0.85 randomly to all coordinates
			for (Object o : os) {
				if (o instanceof Experience) {
					ExperienceOrb orb = l.getWorld().spawn(l, ExperienceOrb.class);
					orb.setExperience(((Experience) o).getXP());
					EffSpawn.lastSpawned = orb;
				} else {
					if (o instanceof ItemStack)
						o = new ItemType((ItemStack) o);
					for (ItemStack is : ((ItemType) o).getItem().getAll()) {
						if (is.getType() != Material.AIR) {
							if (useVelocity) {
								lastSpawned = l.getWorld().dropItemNaturally(itemDropLoc, is);
							} else {
								Item item = l.getWorld().dropItem(l, is);
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
	public String toString(@Nullable Event e, boolean debug) {
		return "drop " + drops.toString(e, debug) + " " + locations.toString(e, debug);
	}

}
