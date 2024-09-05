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
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


@SuppressWarnings("deprecation")
public class EffActionBarTest extends SkriptJUnitTest {

	private Player testPlayer;
	private Player.Spigot testSpigotPlayer;
	private Effect actionBarEffect;

	@Before
	public void setup() {
		testPlayer = EasyMock.niceMock(Player.class);
		testSpigotPlayer = EasyMock.niceMock(Player.Spigot.class);
		actionBarEffect = Effect.parse("send actionbar {_content} to {_player}", null);
	}

	@Test
	public void test() {
		if (actionBarEffect == null)
			Assert.fail("Effect is null");

		String expectedActionBarContent = "hello world";

		EasyMock.expect(testPlayer.spigot()).andAnswer(() -> testSpigotPlayer);

		testSpigotPlayer.sendMessage(
			EasyMock.eq(ChatMessageType.ACTION_BAR),
			(BaseComponent[]) componentMatcher(expectedActionBarContent)
		);

		EasyMock.expectLastCall();

		EasyMock.replay(testPlayer, testSpigotPlayer);

		ContextlessEvent event = ContextlessEvent.get();
		Variables.setVariable("content", expectedActionBarContent, event, true);
		Variables.setVariable("player", testPlayer, event, true);
		TriggerItem.walk(actionBarEffect, event);

		EasyMock.verify(testPlayer, testSpigotPlayer);
	}

	private <T> T componentMatcher(String expectedContent) {
		EasyMock.reportMatcher(new IArgumentMatcher() {
			@Override
			public boolean matches(Object argument) {
				if (argument instanceof TextComponent) {
					return ((TextComponent) argument).getText().equals(expectedContent);
				}
				return false;
			}

			@Override
			public void appendTo(StringBuffer buffer) {
				buffer.append("[component matcher]");
			}
		});

		return null;
	}

}
