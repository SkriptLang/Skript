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
package org.skriptlang.skript.test.tests.classes;

import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryType;
import org.junit.Test;

import ch.njol.skript.entity.CreeperData;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.entity.SimpleEntityData;
import ch.njol.skript.entity.ThrownPotionData;
import ch.njol.skript.entity.WolfData;
import ch.njol.skript.entity.XpOrbData;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.Experience;
import ch.njol.skript.util.SkriptColor;
import ch.njol.skript.util.StructureType;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.WeatherType;

public class ClassesTest {

	@Test
	public void serializationTest() {
		Object[] random = {
				// Java
				(byte) 127, (short) 2000, -1600000, 1L << 40, -1.5f, 13.37,
				"String",
				
				// Skript
				SkriptColor.BLACK, StructureType.RED_MUSHROOM, WeatherType.THUNDER,
				new Date(System.currentTimeMillis()), new Timespan(1337), new Time(12000), new Timeperiod(1000, 23000),
				new Experience(15), new Direction(0, Math.PI, 10), new Direction(new double[] {0, 1, 0}),
				new EntityType(new SimpleEntityData(HumanEntity.class), 300),
				new CreeperData(),
				new SimpleEntityData(Snowball.class),
				new ThrownPotionData(),
				new WolfData(),
				new XpOrbData(50),
				
				// Bukkit - simple classes only
				GameMode.ADVENTURE, InventoryType.CHEST, DamageCause.FALL,
				
				// there is also at least one variable for each class on my test server which are tested whenever the server shuts down.
		};
		for (Object o : random)
			Classes.serialize(o); // includes a deserialisation test
	}

}
