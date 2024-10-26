package org.skriptlang.skript.test.tests.syntaxes.expressions;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.variables.Variables;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ExprLastDeathLocationTest extends SkriptJUnitTest {

	private Player player;
	private Effect get, set;

	@Before
	public void setup() {
		player = EasyMock.niceMock(Player.class);
		set = Effect.parse("set last death location of {_player} to {_location}", null);
		get = Effect.parse("set {_loc} to last death location of {_player}", null);
	}

	@Test
	public void test() {
		if (get == null)
			Assert.fail("Get statement was null");
		if (set == null)
			Assert.fail("Set statement was null");

		Location location = new Location(Bukkit.getWorld("world"), 0, 0, 0);

		ContextlessEvent event = ContextlessEvent.get();
		Variables.setVariable("player", player, event, true);
		Variables.setVariable("location", location, event, true);

		player.setLastDeathLocation(locationMatcher(location));
		EasyMock.expectLastCall();
		EasyMock.replay(player);
		TriggerItem.walk(set, event);
		EasyMock.verify(player);

		EasyMock.resetToNice(player);

		EasyMock.expect(player.getLastDeathLocation()).andReturn(location);
		EasyMock.replay(player);
		TriggerItem.walk(get, event);
		EasyMock.verify(player);
	}

	private <T> T locationMatcher(Location expectedLoc) {
		EasyMock.reportMatcher(new IArgumentMatcher() {
			@Override
			public boolean matches(Object argument) {
				if (argument instanceof Location location) {
					return location.equals(expectedLoc);
				}
				return false;
			}

			@Override
			public void appendTo(StringBuffer buffer) {
				buffer.append("[location matcher]");
			}
		});

		return null;
	}

}
