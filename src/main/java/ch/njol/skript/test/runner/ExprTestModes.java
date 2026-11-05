package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

import java.lang.reflect.Field;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Test Modes")
@Description("Returns the values of TestModes.")
@Examples("set {_values::*} to the values of test modes \"dev_mode\" and \"lastTestFile\"")
@NoDoc
public class ExprTestModes extends SimpleExpression<Object> {

	private static final Field[] FIELDS = TestMode.class.getFields();

	static {
		if (TestMode.ENABLED)
			Skript.registerExpression(ExprTestModes.class, Object.class, ExpressionType.SIMPLE, "[the] [value[s] of] test mode[s] %strings%");
	}

	private Expression<String> fields;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		fields = (Expression<String>) exprs[0];
		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		return fields.stream(event).map(fieldName -> {
			for (Field field : FIELDS) {
				if (field.getName().equalsIgnoreCase(fieldName)) {
					try {
						field.setAccessible(true);
						return field.get(TestMode.get());
					} catch (IllegalArgumentException | IllegalAccessException e) {
						return null;
					}
				}
			}
			return null;
		}).toArray();
	}

	@Override
	public boolean isSingle() {
		return fields.isSingle();
	}

	@Override
	public Class<? extends Object> getReturnType() {
		return Object.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "test mode" + (fields.isSingle() ? " " + fields.toString(event, debug) : "s " + fields.toString(event, debug));
	}

}
