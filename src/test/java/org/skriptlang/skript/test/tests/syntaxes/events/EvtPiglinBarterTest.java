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
package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.Skript;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class EvtPiglinBarterTest extends SkriptJUnitTest {

	private Entity piglin;
	private static final boolean canRun = Skript.classExists("org.bukkit.event.entity.PiglinBarterEvent");

	static {
		setShutdownDelay(1);
	}

	@Before
	public void spawn() {
		if (!canRun)
			return;

		piglin = getTestWorld().spawnEntity(getTestLocation(), EntityType.PIGLIN);
	}

	@Test
	public void testCall() {
		if (!canRun)
			return;

		ItemStack input = new ItemStack(Material.GOLD_INGOT);
		List<ItemStack> outcome = new ArrayList<>();
		outcome.add(new ItemStack(Material.EMERALD));

		try {
			Bukkit.getPluginManager().callEvent(
				new org.bukkit.event.entity.PiglinBarterEvent(
					(org.bukkit.entity.Piglin) piglin, input, outcome));
		} catch (NoClassDefFoundError ignored) { }
	}

	@After
	public void remove() {
		if (!canRun)
			return;

		piglin.remove();
	}

}
