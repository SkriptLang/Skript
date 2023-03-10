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
package org.skriptlang.skript.test.tests.regression;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.njol.skript.test.runner.SkriptJUnitTest;

public class HangingEntity_4871 extends SkriptJUnitTest {

	static {
		// Only needed when we trigger an event or something that isn't instant.
		setShutdownDelay(1);
	}

	private Material before;

	@Before
	public void setupItemFrame() {
		// Ensure it's in the ground for the item frame to attach to a wall.
		Location location = getTestLocation().subtract(0, 2, 0);
		before = location.getBlock().getType();
		location.getBlock().setType(Material.AIR);
		getTestWorld().spawnEntity(location, EntityType.ITEM_FRAME);
	}

	@Test
	public void testDamage() {
		Location location = getTestLocation().subtract(0, 2, 0);
		Optional<ItemFrame> optional = location.getNearbyEntitiesByType(ItemFrame.class, 1).stream().findFirst();
		assert optional.isPresent();
		HangingBreakEvent event = new HangingBreakEvent(optional.get(), RemoveCause.DEFAULT);
		Bukkit.getPluginManager().callEvent(event);
	}

	@After
	public void cleanupItemFrame() {
		Location location = getTestLocation().subtract(0, 2, 0);
		location.getBlock().setType(before);
	}

}
