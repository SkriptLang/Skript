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
package org.skriptlang.skript.test.tests.syntaxes.expressions;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class ExprMessageTest extends SkriptJUnitTest {

	private Player testPlayer;

	@Before
	public void setup() {
		testPlayer = EasyMock.niceMock(Player.class);
	}

	@Test
	public void test() {
		Set<Player> viewers = new HashSet<>();
		viewers.add(testPlayer);
		PluginManager manager = Bukkit.getServer().getPluginManager();
		manager.callEvent(new AsyncPlayerChatEvent(false, testPlayer, "hi", viewers));
		manager.callEvent(new PlayerJoinEvent(testPlayer, "hi"));
		manager.callEvent(new PlayerQuitEvent(testPlayer, "hi"));
	}
}
