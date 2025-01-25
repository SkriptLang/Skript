package org.skriptlang.skript.test.tests.regression;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.variables.Variables;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MissingCheckIfEntityCanUseSlot7524Test extends SkriptJUnitTest {

	private Player player;
	private EntityEquipment equipment;
	private Condition isWearingCondition;

	@Before
	public void setup() {
		player = EasyMock.niceMock(Player.class);
		equipment = EasyMock.niceMock(EntityEquipment.class);

		isWearingCondition = Condition.parse("{_player} is wearing diamond chestplate", null);
		if (isWearingCondition == null)
			throw new IllegalStateException();
	}

	@Test
	public void test() {
		ContextlessEvent event = ContextlessEvent.get();
		Variables.setVariable("player", player, event, true);

		EasyMock.expect(player.isValid()).andStubReturn(true);
		EasyMock.expect(player.getEquipment()).andReturn(equipment);

		EasyMock.expect(player.canUseEquipmentSlot(EquipmentSlot.CHEST)).andReturn(true);
		EasyMock.expect(player.canUseEquipmentSlot(EquipmentSlot.LEGS)).andReturn(true);
		EasyMock.expect(player.canUseEquipmentSlot(EquipmentSlot.FEET)).andReturn(true);
		EasyMock.expect(player.canUseEquipmentSlot(EquipmentSlot.HEAD)).andReturn(true);
		EasyMock.expect(player.canUseEquipmentSlot(EquipmentSlot.HAND)).andReturn(true);
		EasyMock.expect(player.canUseEquipmentSlot(EquipmentSlot.OFF_HAND)).andReturn(true);
		EasyMock.expect(player.canUseEquipmentSlot(EquipmentSlot.BODY)).andReturn(false);

		EasyMock.expect(equipment.getItem(EquipmentSlot.CHEST)).andReturn(new ItemStack(Material.DIAMOND_CHESTPLATE));
		EasyMock.expect(equipment.getItem(EquipmentSlot.LEGS)).andReturn(new ItemStack(Material.DIAMOND_LEGGINGS));
		EasyMock.expect(equipment.getItem(EquipmentSlot.FEET)).andReturn(new ItemStack(Material.DIAMOND_BOOTS));
		EasyMock.expect(equipment.getItem(EquipmentSlot.HEAD)).andReturn(new ItemStack(Material.DIAMOND_HELMET));
		EasyMock.expect(equipment.getItem(EquipmentSlot.HAND)).andReturn(new ItemStack(Material.DIAMOND_SWORD));
		EasyMock.expect(equipment.getItem(EquipmentSlot.OFF_HAND)).andReturn(new ItemStack(Material.DIAMOND_SHOVEL));

		EasyMock.replay(player, equipment);

		isWearingCondition.run(event);

		EasyMock.verify(player, equipment);

		Assert.assertTrue(isWearingCondition.evaluate(event).isTrue());
	}

}
