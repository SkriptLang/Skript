package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ClassInfoReference;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

@Name("Value Within")
@Description(
	"Gets the value within objects. Usually used with variables to get the value they store rather than the variable itself, " +
	"or with lists to get the values of a type."
)
@Example("""
	set {_entity} to a random entity out of all entities
	delete entity within {_entity} # This deletes the entity itself and not the value stored in the variable
	""")
@Example("""
	set {_list::*} to "something", 10, "test" and a zombie
	broadcast the strings within {_list::*} # "something", "test"
	""")
@Since("2.7")
public class ExprValueWithin extends WrapperExpression<Object> implements KeyProviderExpression<Object> {

	static {
		Skript.registerExpression(ExprValueWithin.class, Object.class, ExpressionType.PROPERTY, "[the] (%-*classinfo%|value[:s]) (within|in) %~objects%");
	}

	@Nullable
	private ClassInfo<?> classInfo;

	@Nullable
	@SuppressWarnings("rawtypes")
	private Changer changer;

	private boolean returnsKeys;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		boolean plural;
		if (exprs[0] != null) {
			Literal<ClassInfoReference> classInfoReference = (Literal<ClassInfoReference>) ClassInfoReference.wrap((Expression<ClassInfo<?>>) exprs[0]);
			plural = classInfoReference.getSingle().isPlural().isTrue();
		} else {
			plural = parseResult.hasTag("s");
		}

		if (plural == exprs[1].isSingle()) {
			if (plural) {
				Skript.error("You cannot get multiple elements of a single value");
			} else {
				Skript.error(exprs[1].toString(null, false) + " may contain more than one " + (classInfo == null ? "value" :  classInfo.getName()));
			}
			return false;
		}

		classInfo = exprs[0] == null ? null : ((Literal<ClassInfo<?>>) exprs[0]).getSingle();
		Expression<?> expr = classInfo == null ? exprs[1] : exprs[1].getConvertedExpression(classInfo.getC());
		if (expr == null)
			return false;
		setExpr(expr);
		returnsKeys = KeyProviderExpression.canReturnKeys(expr);
		return true;
	}

	@Override
	public @NotNull String @NotNull [] getArrayKeys(Event event) throws IllegalStateException {
		if (!returnsKeys)
			throw new IllegalStateException();
		return ((KeyProviderExpression<?>) getExpr()).getArrayKeys(event);
	}

	@Override
	public @NotNull String @NotNull [] getAllKeys(Event event) {
		if (!returnsKeys)
			throw new IllegalStateException();
		return ((KeyProviderExpression<?>) getExpr()).getAllKeys(event);
	}

	@Override
	public Iterator<KeyedValue<Object>> keyedIterator(Event event) {
		if (!returnsKeys)
			throw new IllegalStateException();
		//noinspection unchecked
		return ((KeyProviderExpression<Object>) getExpr()).keyedIterator(event);
	}

	@Override
	public boolean canReturnKeys() {
		return returnsKeys;
	}

	@Override
	public boolean areKeysRecommended() {
		return KeyProviderExpression.areKeysRecommended(getExpr());
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		changer = Classes.getSuperClassInfo(getReturnType()).getChanger();
		if (changer == null)
			return null;
		return changer.acceptChange(mode);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (changer == null)
			throw new UnsupportedOperationException();
		changer.change(getArray(event), delta, mode);
	}

	@Override
	public boolean isIndexLoop(String input) {
		if (!returnsKeys)
			throw new IllegalStateException();
		return ((KeyProviderExpression<?>) getExpr()).isIndexLoop(input);
	}

	@Override
	public boolean isLoopOf(String input) {
		return getExpr().isLoopOf(input);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (classInfo == null ? "value" : classInfo.toString(event, debug)) + " within " + getExpr();
	}

}
