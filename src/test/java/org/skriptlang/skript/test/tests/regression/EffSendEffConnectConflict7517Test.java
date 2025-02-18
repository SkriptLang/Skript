package org.skriptlang.skript.test.tests.regression;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.variables.Variables;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EffSendEffConnectConflict7517Test extends SkriptJUnitTest {

	private static final String MESSAGE = "Hello, world!";

	private CommandSender sender;
	private Effect sendEffect;

	@Before
	public void setup() {
		sender = EasyMock.niceMock(CommandSender.class);
		sendEffect = Effect.parse("send {_message} to {_sender}", null);
		if (sendEffect == null)
			throw new IllegalStateException();
	}

	@Test
	public void test() {
		Event event = ContextlessEvent.get();
		Variables.setVariable("sender", sender, event, true);
		Variables.setVariable("message", MESSAGE, event, true);

		Capture<String> messageCapture = EasyMock.newCapture();
		sender.sendMessage(EasyMock.capture(messageCapture));
		EasyMock.replay(sender);

		TriggerItem.walk(sendEffect, event);
		EasyMock.verify(sender);
		Assert.assertEquals(MESSAGE, messageCapture.getValue());
	}

}
