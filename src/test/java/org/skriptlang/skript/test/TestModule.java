package org.skriptlang.skript.test;

import ch.njol.skript.Skript;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.test.elements.conditions.*;
import org.skriptlang.skript.test.elements.effects.*;
import org.skriptlang.skript.test.elements.events.*;
import org.skriptlang.skript.test.elements.expressions.*;
import org.skriptlang.skript.test.elements.sections.*;
import org.skriptlang.skript.test.elements.structures.*;

import java.io.IOException;

public class TestModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		CustomTestOperators.register();
		TestFeatures.register(addon);
		TestFunctions.register(addon);

		register(addon,
			// conditions
			CondHasDelayBefore::register,
			CondMethodExists::register,
			CondRunningJUnit::register,
			// effects
			EffAssert::register,
			EffDebug::register,
			EffObjectives::register,
			EffRunRunnable::register,
			EffTestExprList::register,
			EffTestPluralClassInfos::register,
			// events
			EvtTestCase::register,
			// expressions
			ExprDefaultNumberValue::register,
			ExprExperimentalOnly::register,
			ExprJUnitTestName::register,
			ExprKeyValueSet::register,
			ExprParseLogs::register,
			ExprSecErroringSectionExpression::register,
			ExprSecRunnable::register,
			ExprTestLoopPeeking::register,
			ExprTestStringLiteral::register,
			// sections
			EffSecErrorWhenSection::register,
			SecCustomDefault::register,
			SecParse::register,
			SecReturnable::register,
			// structures
			StructParse::register,
			StructTestEntryContainer::register,
			StructTestSubValidators::register
		);
		try {
			Skript.getAddonInstance().loadClasses("ch.njol.skript.test.runner");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String name() {
		return "testing";
	}

}
