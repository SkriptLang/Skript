package org.skriptlang.skript.test.tests.syntaxes.effects;


import ch.njol.skript.Skript;
import ch.njol.skript.effects.EffApplyBoneMeal;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.variables.Variables;
import org.bukkit.block.Block;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class EffApplyBoneMealTest {

	private Block stubTestBlock;
	private Effect applyBonemealEffect;
	private Effect applyMultipleBonemealEffect;

	@Before
	public void setup() {
		stubTestBlock = EasyMock.niceMock(Block.class);
		applyBonemealEffect = Effect.parse("apply bonemeal to {_block}", null);
		applyMultipleBonemealEffect = Effect.parse("apply {_times} bonemeal to {_block}", null);
	}

	@Test
	public void test() {
		boolean bonemealEffectRegistered = Skript.getEffects().stream()
			.map(SyntaxElementInfo::getElementClass)
			.anyMatch(EffApplyBoneMeal.class::equals);
		if (!bonemealEffectRegistered)
			return;
		if (applyBonemealEffect == null)
			Assert.fail("Effect is null");
		if (applyMultipleBonemealEffect == null)
			Assert.fail("Multiple effect is null");

		int countOfBonemealToApply = 5;
		ContextlessEvent event = ContextlessEvent.get();
		Variables.setVariable("block", getMockBlock(), event, true);
		Variables.setVariable("times", countOfBonemealToApply, event, true);

		EasyMock.expect(stubTestBlock.applyBoneMeal(EasyMock.notNull())).andReturn(true).times(1);
		EasyMock.replay(stubTestBlock);
		TriggerItem.walk(applyBonemealEffect, event);
		EasyMock.verify(stubTestBlock);

		EasyMock.resetToNice(stubTestBlock);
		EasyMock.expect(stubTestBlock.applyBoneMeal(EasyMock.notNull())).andReturn(true).times(2);
		EasyMock.replay(stubTestBlock);
		TriggerItem.walk(applyMultipleBonemealEffect, event);
		EasyMock.verify(stubTestBlock);
	}

	private Block getMockBlock() {
		Block realBlock = SkriptJUnitTest.getBlock();

		// we need to intercept applyBoneMeal calls so that easymock can detect them
		// but we need to pass the other calls to a real block so that a real blockdata,
		// material, location, etc are available
		InvocationHandler handler = (proxy, method, args) -> {
			if (method.getName().equals("applyBoneMeal")) {
				return method.invoke(stubTestBlock, args);
			}
			return method.invoke(realBlock, args);
		};

		return (Block) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Block.class }, handler);
	}
}
