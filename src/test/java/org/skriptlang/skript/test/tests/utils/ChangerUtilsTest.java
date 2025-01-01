package org.skriptlang.skript.test.tests.utils;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.DefaultClasses;
import ch.njol.skript.util.ClassInfoReference;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

public class ChangerUtilsTest {

	@NotNull
	private Expression<?> parseExpression(String expression) {
		ParseResult parseResult = SkriptParser.parse(expression, "%objects%", SkriptParser.ALL_FLAGS, ParseContext.DEFAULT);
		if (parseResult == null) {
			throw new IllegalStateException("Couldn't parse " + expression);
		}
		return parseResult.exprs[0];
	}

	@Test
	public void testReferenceMethod() {
		Expression<?> listVariableExpression = parseExpression("{_list::*}");
		Assert.assertTrue(ChangerUtils.acceptsChange(listVariableExpression, ChangeMode.SET, new ClassInfoReference(DefaultClasses.OBJECT, Kleenean.TRUE)));
		Assert.assertTrue(ChangerUtils.acceptsChange(listVariableExpression, ChangeMode.SET, new ClassInfoReference(DefaultClasses.OBJECT, Kleenean.FALSE)));
		Assert.assertTrue(ChangerUtils.acceptsChange(listVariableExpression, ChangeMode.SET, new ClassInfoReference(DefaultClasses.OBJECT, Kleenean.UNKNOWN)));

		Expression<?> singleVariableExpression = parseExpression("{_var}");
		Assert.assertTrue(ChangerUtils.acceptsChange(singleVariableExpression, ChangeMode.SET, new ClassInfoReference(DefaultClasses.OBJECT, Kleenean.UNKNOWN)));
		Assert.assertTrue(ChangerUtils.acceptsChange(singleVariableExpression, ChangeMode.SET, new ClassInfoReference(DefaultClasses.OBJECT, Kleenean.FALSE)));
		Assert.assertFalse(ChangerUtils.acceptsChange(singleVariableExpression, ChangeMode.SET, new ClassInfoReference(DefaultClasses.OBJECT, Kleenean.TRUE)));

		Expression<?> nameExpression = parseExpression("name of {_something}");
		Assert.assertTrue(ChangerUtils.acceptsChange(nameExpression, ChangeMode.SET, new ClassInfoReference(DefaultClasses.STRING, Kleenean.UNKNOWN)));
		Assert.assertTrue(ChangerUtils.acceptsChange(nameExpression, ChangeMode.SET, new ClassInfoReference(DefaultClasses.STRING, Kleenean.FALSE)));
		Assert.assertFalse(ChangerUtils.acceptsChange(nameExpression, ChangeMode.SET, new ClassInfoReference(DefaultClasses.STRING, Kleenean.TRUE)));

		Expression<?> passengersExpression = parseExpression("passengers of ({_something} and {_another thing})");
		ClassInfo<?> entityClassInfo = Classes.getExactClassInfo(Entity.class);
		if (entityClassInfo == null)
			throw new IllegalStateException("Entity classinfo not found");
		Assert.assertTrue(ChangerUtils.acceptsChange(passengersExpression, ChangeMode.SET, new ClassInfoReference(entityClassInfo, Kleenean.UNKNOWN)));
		Assert.assertTrue(ChangerUtils.acceptsChange(passengersExpression, ChangeMode.SET, new ClassInfoReference(entityClassInfo, Kleenean.FALSE)));
		Assert.assertTrue(ChangerUtils.acceptsChange(passengersExpression, ChangeMode.SET, new ClassInfoReference(entityClassInfo, Kleenean.TRUE)));
	}

}
