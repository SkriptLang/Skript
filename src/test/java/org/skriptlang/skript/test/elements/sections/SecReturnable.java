package org.skriptlang.skript.test.elements.sections;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@NoDoc
public class SecReturnable extends Section implements ReturnHandler<Object> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.SECTION, SyntaxInfo.simple(SecReturnable.class, SecReturnable::new,
			"returnable [:plural] %*classinfo% section"));
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.simple(ExprLastReturnValues.class, ExprLastReturnValues::new, Object.class,
				"[the] last return[ed] value[s]"));
	}

	private ClassInfo<?> returnValueType;
	private boolean singleReturnValue;
	private static Object @Nullable [] returnedValues;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
		//noinspection unchecked
		returnValueType = ((Literal<ClassInfo<?>>) expressions[0]).getSingle();
		singleReturnValue = !parseResult.hasTag("plural");
		loadReturnableSectionCode(sectionNode);
		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		return walk(event, true);
	}

	@Override
	public void returnValues(Event event, Expression<?> value) {
		returnedValues = value.getArray(event);
	}

	@Override
	public boolean isSingleReturnValue() {
		return singleReturnValue;
	}

	@Override
	public @Nullable Class<?> returnValueType() {
		return returnValueType.getC();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "returnable " + (singleReturnValue ? "" : "plural ") + returnValueType.toString(event, debug) + " section";
	}

	@NoDoc
	public static class ExprLastReturnValues extends SimpleExpression<Object> {

		@Override
		public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
			return true;
		}

		@Override
		public @Nullable Object[] get(Event event) {
			Object[] returnedValues = SecReturnable.returnedValues;
			SecReturnable.returnedValues = null;
			return returnedValues;
		}

		@Override
		public boolean isSingle() {
			return false;
		}

		@Override
		public Class<?> getReturnType() {
			return Object.class;
		}

		@Override
		public String toString(@Nullable Event event, boolean debug) {
			return "last returned values";
		}

	}

}
