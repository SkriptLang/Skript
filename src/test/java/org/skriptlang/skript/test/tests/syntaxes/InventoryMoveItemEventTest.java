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
package org.skriptlang.skript.test.tests.syntaxes;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InventoryMoveItemEventTest extends SkriptJUnitTest {

	private Block chest;
	private Block hopper;

	static {
		setShutdownDelay(20);
	}

	@Before
	public void setupContainers() {
		Location testLocation = getBlock().getLocation();
		testLocation.getBlock().setType(Material.CHEST);
		chest = testLocation.getBlock();
		testLocation.subtract(0, -1, 0).getBlock().setType(Material.HOPPER);
		hopper = testLocation.subtract(0, -1, 0).getBlock();
		testLocation.subtract(0, -2, 0).getBlock().setType(Material.AIR);
	}

	@Test
	public void test() {
		((Chest) chest.getState()).getBlockInventory().addItem(new ItemStack(Material.STONE));
	}

	@After
	public void clearContainers() {
		hopper.setType(Material.AIR);
	}

}
