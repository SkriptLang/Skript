package ch.njol.skript.expressions.variable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.variable.VariableManager;
import ch.njol.skript.lang.variable.VariableType;
import ch.njol.skript.registrations.Feature;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.regex.Pattern;

@NoDoc
public abstract class ExprAbstractVariable extends SimpleExpression<Object> {

	public static final Pattern VARIABLE_NAME = Pattern.compile("\\w{1,20}");

	protected static String pattern(VariableType type) {
		return type.prefix() + "<" + VARIABLE_NAME.pattern() + ">";
	}

	protected String name;
	protected ClassInfo<?> type;
	protected VariableManager manager;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result) {
		if (!this.getParser().hasExperiment(Feature.VARIABLES))
			return false;
		this.name = result.regexes.getFirst().group().trim();
		if (!this.init(pattern, name))
			return false;
		if (name == null || type == null)
			throw new IllegalStateException("Extending class did not configure variable details properly.");
		this.manager = Skript.getInstance().variablesManager();
		return true;
	}

	public abstract boolean init(int pattern, String name);

	@Override
	public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
		return switch (mode) {
			case SET -> CollectionUtils.array(this.getReturnType());
			case DELETE -> CollectionUtils.array();
			default -> this.acceptElementChange(mode);
		};
	}

	protected Class<?> @Nullable [] acceptElementChange(Changer.ChangeMode mode) {
		Changer<?> changer = type.getChanger();
		if (changer == null)
			return null;
		return changer.acceptChange(mode);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
		assert manager != null;
		switch (mode) {
			case SET -> {
				assert delta != null;
				manager.set(this.variableType(), name, this.coerce(delta[0]));
			}
			case DELETE -> manager.delete(this.variableType(), name);
		}
		// todo
		super.change(event, delta, mode);
	}

	public void elementChange(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
		//noinspection rawtypes
		Changer changer = type.getChanger();
		if (changer == null)
			throw new UnsupportedOperationException();
		//noinspection unchecked
		changer.change(this.getRaw(event), delta, mode);
	}

	protected Object coerce(Object value) {
		Class<?> type = this.type.getC();
		if (value == null || type.isInstance(value))
			return value;
		return Converters.convert(value, type);
	}

	public Object[] getRaw(Event event) {
		assert manager != null;
		return new Object[] {manager.get(this.variableType(), name)};
	}

	public abstract VariableType variableType();

	public String prefix() {
		return this.variableType().prefix();
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		return type.getC();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return this.prefix() + name;
	}

}
