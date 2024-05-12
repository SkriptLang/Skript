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
import org.bukkit.OfflinePlayer;
import org.junit.Test;

public class EvtWhitelistTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(1);
	}

	private static final boolean HAS_SERVER_WHITELIST_EVENT = Skript.classExists("com.destroystokyo.paper.event.server.WhitelistToggleEvent");
	private static final boolean HAS_PLAYER_WHITELIST_EVENT = Skript.classExists("io.papermc.paper.event.server.WhitelistStateUpdateEvent");

	@Test
	public void whitelist() {
		// Test player whitelist event
		if (HAS_PLAYER_WHITELIST_EVENT) {
			OfflinePlayer player = Bukkit.getOfflinePlayer("Njol");
			player.setWhitelisted(true);
			player.setWhitelisted(false);
		}

		// Test server whitelist event
		if (HAS_SERVER_WHITELIST_EVENT) {
			Bukkit.setWhitelist(true);
			Bukkit.setWhitelist(false);
		}
	}

	@Override
	public void cleanup() {
		Bukkit.setWhitelist(false);
		for (OfflinePlayer player : Bukkit.getWhitelistedPlayers()) {
			player.setWhitelisted(false);
		}
	}

}
