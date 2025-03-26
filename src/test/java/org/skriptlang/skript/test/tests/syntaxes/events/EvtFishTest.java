package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.Skript;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerFishEvent;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class EvtFishTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(1);
	}

	private Fish salmon;
	private Fish cod;

	@Before
	public void setup() {
		salmon = getTestLocation().getWorld().spawn(getTestLocation(), Salmon.class);
		cod = getTestLocation().getWorld().spawn(getTestLocation(), Cod.class);
	}

	@Test
	public void test() {
		Player player = EasyMock.niceMock(Player.class);
		EasyMock.expect(player.getName()).andReturn("Efnilite").anyTimes();
		EasyMock.replay(player);

		Bukkit.getPluginManager().callEvent(new PlayerFishEvent(
			player, salmon, new FishHookMock(salmon).getHook(), PlayerFishEvent.State.CAUGHT_FISH));

		Bukkit.getPluginManager().callEvent(new PlayerFishEvent(
			player, cod, new FishHookMock(cod).getHook(), PlayerFishEvent.State.FISHING));
	}

	@After
	public void reset() {
		salmon.remove();
		cod.remove();
	}

	@Ignore
	private static class FishHookMock {

		private final FishHook hook;
		private int maxWaitTime = 30 * 20;
		private int minWaitTime = 5 * 20;
		private float minApproachAngle = 0;
		private float maxApproachAngle = 360;
		private boolean lure = false;

		public FishHookMock(Entity hooked) {
			hook = EasyMock.niceMock(FishHook.class);

			EasyMock.expect(hook.isInOpenWater()).andReturn(false).anyTimes();

			EasyMock.expect(hook.getHookedEntity()).andAnswer(() -> hooked).anyTimes();
			EasyMock.expect(hook.getMaxWaitTime()).andAnswer(() -> maxWaitTime).anyTimes();
			EasyMock.expect(hook.getMinWaitTime()).andAnswer(() -> minWaitTime).anyTimes();
			EasyMock.expect(hook.getMinLureAngle()).andAnswer(() -> minApproachAngle).anyTimes();
			EasyMock.expect(hook.getMaxLureAngle()).andAnswer(() -> maxApproachAngle).anyTimes();
			EasyMock.expect(hook.getApplyLure()).andAnswer(() -> lure).anyTimes();

			hook.setMaxWaitTime(EasyMock.anyInt());
			EasyMock.expectLastCall().andAnswer(() -> {
				maxWaitTime = (int) EasyMock.getCurrentArguments()[0];
				return null;
			}).anyTimes();

			hook.setMinWaitTime(EasyMock.anyInt());
			EasyMock.expectLastCall().andAnswer(() -> {
				minWaitTime = (int) EasyMock.getCurrentArguments()[0];
				return null;
			}).anyTimes();

			hook.setMinLureAngle(EasyMock.anyFloat());
			EasyMock.expectLastCall().andAnswer(() -> {
				minApproachAngle = (float) EasyMock.getCurrentArguments()[0];
				return null;
			}).anyTimes();

			hook.setMaxLureAngle(EasyMock.anyFloat());
			EasyMock.expectLastCall().andAnswer(() -> {
				maxApproachAngle = (float) EasyMock.getCurrentArguments()[0];
				return null;
			}).anyTimes();

			hook.setApplyLure(EasyMock.anyBoolean());
			EasyMock.expectLastCall().andAnswer(() -> {
				lure = (boolean) EasyMock.getCurrentArguments()[0];
				return null;
			}).anyTimes();

			EasyMock.replay(hook);
		}

		public FishHook getHook() {
			return hook;
		}

	}

}
