package ch.njol.skript.lang.function;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.util.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link JavaFunction} which doesn't make use of
 * the {@link FunctionEvent} instance and that cannot
 * accept empty / {@code null} parameters.
 */
public abstract class SimpleJavaFunction<T> extends JavaFunction<T> {
	
	public SimpleJavaFunction(Signature<T> sign) {
		super(sign);
	}
	
	public SimpleJavaFunction(String name, Parameter<?>[] parameters, ClassInfo<T> returnType, boolean single) {
		super(name, parameters, returnType, single);
	}

	public SimpleJavaFunction(String name, Parameter<?>[] parameters, ClassInfo<T> returnType, boolean single, Contract contract) {
		super(name, parameters, returnType, single, contract);
	}

	@Override
	public final T @Nullable [] execute(FunctionEvent<?> event, Object[][] params) {
		for (Object[] param : params) {
			if (param == null || param.length == 0 || param[0] == null)
				return null;
		}
		return executeSimple(params);
	}

	public abstract T @Nullable [] executeSimple(Object[][] params);
	
}
