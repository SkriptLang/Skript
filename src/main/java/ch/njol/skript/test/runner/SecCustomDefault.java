package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.parser.DefaultValueData;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SecCustomDefault extends Section {

	static {
		if (TestMode.ENABLED) {
			Skript.registerSection(SecCustomDefault.class, "run with custom default value %*object% for %*classinfo%");
		}
	}

	Literal<?> value;
	ClassInfo<?> type;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult,
						SectionNode sectionNode, List<TriggerItem> triggerItems) {
		value = (Literal<?>) LiteralUtils.defendExpression(expressions[0]);
		//noinspection unchecked
		this.type = ((Literal<ClassInfo<?>>) expressions[1]).getSingle();
		Class<?> type = this.type.getC();

		if (!type.isAssignableFrom(value.getReturnType())) {
			Skript.error("The value expression returns an invalid type: expected " + type.getSimpleName() +
				", got " + value.getReturnType().getSimpleName());
			return true;
		}

		DefaultValueData data = getParser().getData(DefaultValueData.class);

		// Add custom default value
		//noinspection rawtypes,unchecked
		data.addDefaultValue((Class) type, (DefaultExpression) value);

		// parse section with custom value
		loadCode(sectionNode);

		// remove custom value
		data.removeDefaultValue(type);

		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		return walk(event, true);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "run with custom default value " + value.toString(event, debug) + " for " + type.toString(event, debug);
	}

}
