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

import static org.easymock.EasyMock.createMock;

import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.junit.Test;

import com.google.common.collect.Sets;

import ch.njol.skript.test.runner.SkriptJUnitTest;

/**
 * Issue #5689 caused an exception when attempting to do a sync required actions on an async event.
 */
@SuppressWarnings("deprecation") // Paper wants AsyncChatEvent for AdventureAPI
public class AsyncEvents_5689 extends SkriptJUnitTest {

	private static Player njol = createMock(Player.class);

	@Test
	public void execute() {
		AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, njol, "Issue 5689", Sets.newHashSet(njol));
		CompletableFuture.runAsync(() -> Bukkit.getPluginManager().callEvent(event));
	}

}
