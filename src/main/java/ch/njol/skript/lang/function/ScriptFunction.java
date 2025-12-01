package ch.njol.skript.lang.function;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.variables.HintManager;
import ch.njol.skript.variables.Variables;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

import java.util.Arrays;

public class ScriptFunction<T> extends Function<T> implements ReturnHandler<T> {

	private final Trigger trigger;

	private final ThreadLocal<Boolean> returnValueSet = ThreadLocal.withInitial(() -> false);
	private final ThreadLocal<T @Nullable []> returnValues = new ThreadLocal<>();
	private final ThreadLocal<String @Nullable []> returnKeys = new ThreadLocal<>();

	/**
	 * @deprecated use {@link ScriptFunction#ScriptFunction(Signature, SectionNode)} instead.
	 */
	@Deprecated(since = "2.9.0", forRemoval = true)
	public ScriptFunction(Signature<T> sign, Script script, SectionNode node) {
		this(sign, node);
	}

	public ScriptFunction(Signature<T> sign, SectionNode node) {
		super(sign);

		Functions.currentFunction = this;
		HintManager hintManager = ParserInstance.get().getHintManager();
		try {
			hintManager.enterScope(false);
			for (Parameter<?> parameter : sign.getParameters()) {
				String hintName = parameter.name();
				if (!parameter.isSingleValue()) {
					hintName += Variable.SEPARATOR + "*";
				}
				hintManager.set(hintName, parameter.type());
			}
			trigger = loadReturnableTrigger(node, "function " + sign.getName(), new SimpleEvent());
		} finally {
			hintManager.exitScope();
			Functions.currentFunction = null;
		}
		trigger.setLineNumber(node.getLine());
	}

	// REMIND track possible types of local variables (including undefined variables) (consider functions, commands, and EffChange) - maybe make a general interface for this purpose
	// REM: use patterns, e.g. {_a%b%} is like "a.*", and thus subsequent {_axyz} may be set and of that type.
	@Override
	public T @Nullable [] execute(FunctionEvent<?> event, Object[][] params) {
		Parameter<?>[] parameters = getSignature().getParameters();
		for (int i = 0; i < parameters.length; i++) {
			Parameter<?> parameter = parameters[i];
			Object[] val = params[i];
			if (parameter.single && val.length > 0) {
				Variables.setVariable(parameter.name, val[0], event, true);
				continue;
			}

			boolean keyed = Arrays.stream(val).allMatch(it -> it instanceof KeyedValue<?>);
			if (keyed) {
				for (Object value : val) {
					KeyedValue<?> keyedValue = (KeyedValue<?>) value;
					Variables.setVariable(parameter.name + Variable.SEPARATOR + keyedValue.key(), keyedValue.value(), event, true);
				}
			} else {
				int count = 0;
				for (Object value : val) {
					// backup for if the passed argument is not a keyed value.
					// an example of this is passing `xs: integers = (1, 2)` as a parameter.
					Variables.setVariable(parameter.name + Variable.SEPARATOR + count, value, event, true);
					count++;
				}
			}
		}

		trigger.execute(event);
		ClassInfo<T> returnType = getReturnType();
		return returnType != null ? returnValues.get() : null;
	}

	@Override
	public @NotNull String @Nullable [] returnedKeys() {
		return returnKeys.get();
	}

	/**
	 * @deprecated Use {@link ScriptFunction#returnValues(Event, Expression)} instead.
	 */
	@Deprecated(since = "2.9.0", forRemoval = true)
	@ApiStatus.Internal
	public final void setReturnValue(@Nullable T[] values) {
		assert !returnValueSet.get();
		returnValueSet.set(true);
		this.returnValues.set(values);
	}

	@Override
	public boolean resetReturnValue() {
		returnValueSet.remove();
		returnValues.remove();
		returnKeys.remove();
		return true;
	}

	@Override
	public final void returnValues(Event event, Expression<? extends T> value) {
		assert !returnValueSet.get();
		returnValueSet.set(true);
		this.returnValues.set(value.getArray(event));
		if (KeyProviderExpression.canReturnKeys(value))
			this.returnKeys.set(((KeyProviderExpression<?>) value).getArrayKeys(event));
	}

	@Override
	public final boolean isSingleReturnValue() {
		return isSingle();
	}

	@Override
	public final @Nullable Class<? extends T> returnValueType() {
		return getReturnType() != null ? getReturnType().getC() : null;
	}

}
