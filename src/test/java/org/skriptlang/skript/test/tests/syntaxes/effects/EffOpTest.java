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
package org.skriptlang.skript.test.tests.syntaxes.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.variables.Variables;
import org.bukkit.entity.Player;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EffOpTest extends SkriptJUnitTest {

	private Player testPlayer;
	private Effect opPlayerEffect;
	private Effect deopPlayerEffect;

	@Before
	public void setup() {
		testPlayer = EasyMock.niceMock(Player.class);
		opPlayerEffect = Effect.parse("op {_player}", null);
		deopPlayerEffect = Effect.parse("deop {_player}", null);
	}

	@Test
	public void test() {
		if (opPlayerEffect == null)
			Assert.fail("Op player effect is null");
		if (deopPlayerEffect == null)
			Assert.fail("Deop player effect is null");

		ContextlessEvent event = ContextlessEvent.get();
		Variables.setVariable("player", testPlayer, event, true);

		testPlayer.setOp(true);
		EasyMock.expectLastCall();
		EasyMock.replay(testPlayer);
		TriggerItem.walk(opPlayerEffect, event);
		EasyMock.verify(testPlayer);

		EasyMock.resetToNice(testPlayer);
		testPlayer.setOp(false);
		EasyMock.expectLastCall();
		EasyMock.replay(testPlayer);
		TriggerItem.walk(deopPlayerEffect, event);
		EasyMock.verify(testPlayer);
	}

}
