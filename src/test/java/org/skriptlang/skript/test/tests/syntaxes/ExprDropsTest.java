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
import org.bukkit.entity.Pig;
import org.junit.Before;
import org.junit.Test;

public class ExprDropsTest extends SkriptJUnitTest {

	private Pig pig;

	static {
		setShutdownDelay(1);
	}

	@Before
	public void spawnPig() {
		pig = spawnTestPig();
	}

	@Test
	public void killPig() {
		pig.damage(100);
	}

}
