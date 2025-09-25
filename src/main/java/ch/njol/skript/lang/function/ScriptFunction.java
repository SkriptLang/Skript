package ch.njol.skript.lang.function;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.variables.HintManager;
import ch.njol.skript.variables.Variables;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.function.FunctionArguments;
import org.skriptlang.skript.common.function.Parameter;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class ScriptFunction<T> extends Function<T> implements ReturnHandler<T> {

	private final Trigger trigger;

	private boolean returnValueSet;
	private T @Nullable [] returnValues;
	private String @Nullable [] returnKeys;

	public ScriptFunction(Signature<T> sign, SectionNode node) {
		super(sign);

		Functions.currentFunction = this;
		HintManager hintManager = ParserInstance.get().getHintManager();
		try {
			hintManager.enterScope(false);
			for (Parameter<?> parameter : sign.parameters().values()) {
				String hintName = parameter.name();
				if (!parameter.single()) {
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
		LinkedHashMap<String, Parameter<?>> parameters = getSignature().parameters();

		int i = 0;
		for (Entry<String, Parameter<?>> entry : parameters.entrySet()) {
			Parameter<?> parameter = entry.getValue();

			Object[] val = params[i];
			if (parameter.single() && val.length > 0) {
				Variables.setVariable(parameter.name(), val[0], event, true);
			} else {
				for (Object value : val) {
					KeyedValue<?> keyedValue = (KeyedValue<?>) value;
					Variables.setVariable(parameter.name() + "::" + keyedValue.key(), keyedValue.value(), event, true);
				}
			}
			i++;
		}

		trigger.execute(event);
		return returnType() != null ? returnValues : null;
	}

	@Override
	public T execute(org.skriptlang.skript.common.function.FunctionEvent<?> event, FunctionArguments arguments) {
		LinkedHashMap<String, Parameter<?>> parameters = getSignature().parameters();
		FunctionEvent<?> newEvent = new FunctionEvent<>(this);

		for (String name : arguments.names()) {
			Parameter<?> parameter = parameters.get(name);
			Object value = arguments.get(name);

			if (value == null) {
				continue;
			}

			if (parameter.single()) {
				Variables.setVariable(name, value, newEvent, true);
			} else {
				if (value instanceof KeyedValue<?>[] keyedValues) {
					for (KeyedValue<?> keyedValue : keyedValues) {
						Variables.setVariable(name + "::" + keyedValue.key(), keyedValue.value(), newEvent, true);
					}
				} else {
					int i = 0;
					for (Object o : (Object[]) value) {
						Variables.setVariable(name + "::" + i, o, newEvent, true);
						i++;
					}
				}
			}
		}

		trigger.execute(newEvent);

		if (returnType() == null || returnValues == null || returnValues.length == 0) {
			return null;
		}

		if (returnValues.length == 1) {
			return returnValues[0];
		} else {
			//noinspection unchecked
			return (T) returnValues;
		}
	}

	@Override
	public @NotNull String @Nullable [] returnedKeys() {
		return returnKeys;
	}

	@Override
	public boolean resetReturnValue() {
		returnValueSet = false;
		returnValues = null;
		returnKeys = null;
		return true;
	}

	@Override
	public final void returnValues(Event event, Expression<? extends T> value) {
		assert !returnValueSet;
		returnValueSet = true;
		this.returnValues = value.getArray(event);
		if (KeyProviderExpression.canReturnKeys(value))
			this.returnKeys = ((KeyProviderExpression<?>) value).getArrayKeys(event);
	}

	@Override
	public final boolean isSingleReturnValue() {
		return isSingle();
	}

	@Override
	public final @Nullable Class<? extends T> returnValueType() {
		if (returnType() == null) {
			return null;
		}

		if (returnType().isArray()) {
			//noinspection unchecked
			return (Class<? extends T>) returnType().componentType();
		} else {
			return returnType();
		}
	}

}
