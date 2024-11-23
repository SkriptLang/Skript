package org.skriptlang.skript.test.utils;

import org.bukkit.Input;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.easymock.EasyMock;
import org.skriptlang.skript.bukkit.input.InputKey;

public class InputHelper {

	public static PlayerEvent createPlayerInputEvent(Player player, InputKey... keys) {
		return new PlayerInputEvent(player, fromKeys(keys));
	}

	public static Input fromKeys(InputKey... keys) {
		Input input = EasyMock.niceMock(Input.class);
		for (InputKey key : keys) {
			switch (key) {
				case FORWARD -> EasyMock.expect(input.isForward()).andStubReturn(true);
				case BACKWARD -> EasyMock.expect(input.isBackward()).andStubReturn(true);
				case RIGHT -> EasyMock.expect(input.isRight()).andStubReturn(true);
				case LEFT -> EasyMock.expect(input.isLeft()).andStubReturn(true);
				case JUMP -> EasyMock.expect(input.isJump()).andStubReturn(true);
				case SNEAK -> EasyMock.expect(input.isSneak()).andStubReturn(true);
				case SPRINT -> EasyMock.expect(input.isSprint()).andStubReturn(true);
			}
		}
		EasyMock.replay(input);
		return input;
	}

}
