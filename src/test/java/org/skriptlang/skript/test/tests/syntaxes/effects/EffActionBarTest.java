package org.skriptlang.skript.test.tests.syntaxes.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.variables.Variables;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EffActionBarTest extends SkriptJUnitTest {

	private static final Component MESSAGE = Component.text("Hello, world!");

	private CommandSender sender;
	private Effect actionBarEffect;

	@Before
	public void setup() {
		sender = EasyMock.createMock(CommandSender.class);
		actionBarEffect = Effect.parse("send actionbar {_message} to {_sender}", null);
		if (actionBarEffect == null)
			throw new IllegalStateException();
	}

	@Test
	public void test() {
		Event event = ContextlessEvent.get();
		Variables.setVariable("message", MESSAGE, event, true);
		Variables.setVariable("sender", sender, event, true);

		Capture<Component> messageCapture = EasyMock.newCapture();
		sender.sendActionBar(EasyMock.capture(messageCapture));
		EasyMock.replay(sender);

		TriggerItem.walk(actionBarEffect, event);
		EasyMock.verify(sender);
		Assert.assertEquals(MESSAGE, messageCapture.getValue());
	}

}
