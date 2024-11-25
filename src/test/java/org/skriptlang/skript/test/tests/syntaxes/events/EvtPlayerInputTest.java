package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.Skript;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.bukkit.input.InputKey;
import org.skriptlang.skript.test.utils.InputHelper;

public class EvtPlayerInputTest extends SkriptJUnitTest {

	private static final boolean SUPPORTS_INPUT_EVENT = Skript.classExists("org.bukkit.event.player.PlayerInputEvent");

	static {
		setShutdownDelay(1);
	}

	private Player player;

	@Before
	public void setup() {
		if (!SUPPORTS_INPUT_EVENT)
			return;
		player = EasyMock.niceMock(Player.class);
	}

	@Test
	public void test() {
		if (!SUPPORTS_INPUT_EVENT)
			return;
		EasyMock.expect(player.getCurrentInput()).andStubReturn(InputHelper.fromKeys(InputKey.FORWARD));
		EasyMock.replay(player);
		Bukkit.getPluginManager().callEvent(InputHelper.createPlayerInputEvent(player, InputKey.FORWARD, InputKey.JUMP));
		EasyMock.verify(player);
	}

}
