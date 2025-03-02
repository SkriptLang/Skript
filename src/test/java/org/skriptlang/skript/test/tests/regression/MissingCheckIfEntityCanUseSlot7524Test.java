package org.skriptlang.skript.test.tests.regression;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.variables.Variables;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MissingCheckIfEntityCanUseSlot7524Test extends SkriptJUnitTest {

	private static final boolean HAS_CAN_USE_SLOT_METHOD = Skript.methodExists(LivingEntity.class, "canUseEquipmentSlot", EquipmentSlot.class);

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

		if (HAS_CAN_USE_SLOT_METHOD) {
			EasyMock.expect(player.canUseEquipmentSlot(EquipmentSlot.CHEST)).andReturn(true);
			EasyMock.expect(player.canUseEquipmentSlot(EquipmentSlot.LEGS)).andReturn(true);
			EasyMock.expect(player.canUseEquipmentSlot(EquipmentSlot.FEET)).andReturn(true);
			EasyMock.expect(player.canUseEquipmentSlot(EquipmentSlot.HEAD)).andReturn(true);
			EasyMock.expect(player.canUseEquipmentSlot(EquipmentSlot.HAND)).andReturn(true);
			EasyMock.expect(player.canUseEquipmentSlot(EquipmentSlot.OFF_HAND)).andReturn(true);
			EasyMock.expect(player.canUseEquipmentSlot(EquipmentSlot.BODY)).andReturn(false);
		}

		EasyMock.expect(equipment.getItem(EquipmentSlot.CHEST)).andReturn(new ItemStack(Material.DIAMOND_CHESTPLATE));

		EasyMock.replay(player, equipment);

		assert isWearingCondition.check(event);

		EasyMock.verify(player, equipment);
	}

}
