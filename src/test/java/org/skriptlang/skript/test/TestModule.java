package org.skriptlang.skript.test;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.test.elements.conditions.*;
import org.skriptlang.skript.test.elements.effects.*;
import org.skriptlang.skript.test.elements.expressions.*;
import org.skriptlang.skript.test.elements.sections.*;
import org.skriptlang.skript.test.elements.structures.*;

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
			StructTestCase::register,
			StructParse::register,
			StructTestEntryContainer::register,
			StructTestSubValidators::register
		);
	}

	@Override
	public String name() {
		return "testing";
	}

}
