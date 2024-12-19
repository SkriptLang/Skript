package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.Skript;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import io.papermc.paper.event.block.BeaconActivatedEvent;
import io.papermc.paper.event.block.BeaconDeactivatedEvent;
import io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EvtBeaconTest extends SkriptJUnitTest {

	private Block beacon;
	private Beacon state;
	private final PotionEffectType effectType = PotionEffectType.HEALTH_BOOST;
	private final PotionEffectType effectType2 = PotionEffectType.REGENERATION;
	private final PotionEffect effect = new PotionEffect(effectType, 300, 1);
	private final PotionEffect effect2 = new PotionEffect(effectType2, 300, 1);

	private Player easyMockPlayer;

	@Before
	public void setUp() {
		beacon = setBlock(Material.BEACON);
		beacon.setType(Material.BEACON);
		state = (Beacon) beacon.getState();
		state.setPrimaryEffect(effectType);
		state.setSecondaryEffect(effectType2);
		state.update(true);
		easyMockPlayer = EasyMock.niceMock(Player.class);
	}

	@Test
	public void callEvents() {
		BeaconEffectEvent beaconEffectEvent = new BeaconEffectEvent(beacon, effect, easyMockPlayer, true);
		BeaconEffectEvent beaconEffectEvent2 = new BeaconEffectEvent(beacon, effect2, easyMockPlayer, false);
		BeaconActivatedEvent beaconActivatedEvent = new BeaconActivatedEvent(beacon);
		BeaconDeactivatedEvent beaconDeactivatedEvent = new BeaconDeactivatedEvent(beacon);
		PlayerChangeBeaconEffectEvent playerChangeBeaconEffectEvent = new PlayerChangeBeaconEffectEvent(easyMockPlayer, effectType, effectType2, beacon);

		Bukkit.getPluginManager().callEvent(beaconEffectEvent);
		Bukkit.getPluginManager().callEvent(beaconEffectEvent2);
		Bukkit.getPluginManager().callEvent(beaconActivatedEvent);
		Bukkit.getPluginManager().callEvent(beaconDeactivatedEvent);
		Bukkit.getPluginManager().callEvent(playerChangeBeaconEffectEvent);
	}

	@After
	public void cleanup() {
		if (beacon != null)
			beacon.setType(Material.AIR);
	}

}
