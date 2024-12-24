package ch.njol.skript.expressions;

import java.util.Iterator;
import java.util.function.Supplier;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;

@Name("Sets")
@Description("Returns a list of all the values of a type. Useful for looping.")
@Examples({
	"loop all attribute types:",
		"\tset loop-value attribute of player to 10",
		"\tmessage \"Set attribute %loop-value% to 10!\""
})
// Class history rename order: LoopItems.class -> ExprItems.class (renamed in 2.3-alpha1) -> ExprSets.class (renamed in 2.7.0)
@Since("1.0 pre-5, 2.7 (classinfo)")
public class ExprSets extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprSets.class, Object.class, ExpressionType.PATTERN_MATCHES_EVERYTHING,
				"[all [[of] the]|the|every] %*classinfo%");
	}

	@Nullable
	private Supplier<? extends Iterator<?>> supplier;
	private ClassInfo<?> classInfo;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		// This check makes sure that "color" is not a valid pattern, and the type the user inputted has to be plural, unless it's "every %classinfo%"
		boolean plural = Utils.getEnglishPlural(parser.expr).getSecond();
		if (!plural && !parser.expr.startsWith("every"))
			return false;

		classInfo = ((Literal<ClassInfo<?>>) exprs[0]).getSingle();
		supplier = classInfo.getSupplier();
		if (supplier == null) {
			Skript.error("You cannot get all values of type '" + classInfo.getName().getSingular() + "'");
			return false;
		}
		return true;
	}

	@Override
	protected Object[] get(Event event) {
		Iterator<?> iterator = supplier.get();
		return Lists.newArrayList(iterator).toArray(new Object[0]);
	}

	@Override
	@Nullable
	public Iterator<?> iterator(Event event) {
		return supplier.get();
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<?> getReturnType() {
		return classInfo.getC();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "all of the " + classInfo.getName().getPlural();
	}

}
