/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.lang.function;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.SkriptCommand;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Contract;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.util.Executable;
import org.skriptlang.skript.util.Validated;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public class DynamicFunctionReference<Result>
	implements Contract, Executable<Event, Result[]>, Validated {

	private final @NotNull String name;
	private final @Nullable Script source;
	private final Reference<Function<? extends Result>> function;
	private final @UnknownNullability Signature<? extends Result> signature;
	private final Validated validator = Validated.validator();
	private final boolean resolved;

	public DynamicFunctionReference(Function<? extends Result> function) {
		this.resolved = true;
		this.function = new WeakReference<>(function);
		this.name = function.getName();
		this.signature = function.getSignature();
		@Nullable File file = SkriptCommand.getScriptFromName(signature.script);
		if (file == null)
			this.source = null;
		else
			this.source = ScriptLoader.getScript(file);
	}

	public DynamicFunctionReference(@NotNull String name) {
		this(name, null);
	}

	public DynamicFunctionReference(@NotNull String name, @Nullable Script source) {
		this.name = name;
		Function<? extends Result> function;
		if (source != null)
			//noinspection unchecked
			function = (Function<? extends Result>) Functions.getFunction(name, source.getConfig().getFileName());
		else
			//noinspection unchecked
			function = (Function<? extends Result>) Functions.getFunction(name, null);
		this.resolved = function != null;
		this.function = new WeakReference<>(function);
		if (resolved) {
			this.signature = function.getSignature();
			@Nullable File file = SkriptCommand.getScriptFromName(signature.script);
			if (file == null)
				this.source = null;
			else
				this.source = ScriptLoader.getScript(file);
		} else {
			this.signature = null;
			this.source = null;
		}
	}

	public @Nullable Script source() {
		return source;
	}

	public String name() {
		return name;
	}

	@Override
	public boolean isSingle(Expression<?>... arguments) {
		if (!resolved)
			return true;
		return signature.contract != null
				? signature.contract.isSingle(arguments)
				: signature.isSingle();
	}

	@Override
	public @Nullable Class<?> getReturnType(Expression<?>... arguments) {
		if (!this.valid())
			return Object.class;
		if (signature.contract != null)
			return signature.contract.getReturnType(arguments);
		Function<? extends Result> function = this.function.get();
		if (function == null)
			return null;
		if (function.getReturnType() != null)
			return function.getReturnType().getC();
		return null;
	}

	@Override
	public Result @Nullable [] execute(Event event, Object... arguments) {
		if (!this.valid())
			return null;
		Function<? extends Result> function = this.function.get();
		if (function == null)
			return null;
		// We shouldn't trust the caller provided an array of arrays
		Object[][] consigned = FunctionReference.consign(arguments);
		try {
			return function.execute(consigned);
		} finally {
			function.resetReturnValue();
		}
	}

	@Override
	public void invalidate() throws UnsupportedOperationException {
		this.validator.invalidate();
	}

	@Override
	public boolean valid() {
		return resolved && validator.valid()
			&& function.get() != null // function was garbage-collected
			&& (source == null || source.valid());
		// if our source script has been reloaded our reference was invalidated
	}

	@Override
	public String toString() {
		if (source != null)
			return name + "() from " + Classes.toString(source);
		return name + "()";
	}

}
