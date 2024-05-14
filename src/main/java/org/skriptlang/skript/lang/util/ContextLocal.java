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
package org.skriptlang.skript.lang.util;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A context (and thread)-local reference.
 * This is designed for storing event/context-relative values, but is also safe for
 * multiple of the same context running across parallel threads.
 * @param <Context> The context, e.g. an event.
 * @param <Type> The type of value stored in this.
 */
@SuppressWarnings("ClassEscapesDefinedScope")
public class ContextLocal<Context, Type> extends ThreadLocal<Type> {

	private final ThreadLocal<Map<@Nullable Context, Type>> local;
	private final ContextDependentSupplierFunction<Context, Type> initialValue;

	@Override
	protected Type initialValue() {
		return this.initialValue.get();
	}

	protected Type initialValue(Context context) {
		return this.initialValue.apply(context);
	}

	public ContextLocal(ContextIndependentSupplierFunction<Context, Type> initialValue) {
		this(initialValue::apply);
	}

	public ContextLocal(ContextDependentSupplierFunction<Context, Type> initialValue) {
		super();
		this.initialValue = initialValue;
		this.local = InheritableThreadLocal.withInitial(WeakHashMap::new);
	}

	public ContextLocal() {
		this(() -> null);
	}

	private Map<@Nullable Context, Type> map() {
		return local.get();
	}

	@Override
	public Type get() {
		return this.get(null);
	}

	public Type get(@Nullable Context context) {
		return this.map().computeIfAbsent(context, initialValue);
	}

	@Override
	public void set(Type value) {
		this.set(null, value);
	}

	public void set(@Nullable Context context, Type value) {
		this.map().put(context, value);
	}

	@Override
	public void remove() {
		this.remove(null);
	}

	public void remove(@Nullable Context context) {
		this.map().remove(context);
	}

	@FunctionalInterface
	public interface ContextIndependentSupplierFunction<Context, Type>
		extends Supplier<Type>, Function<Context, Type> {

		@Override
		default Type apply(Context context) {
			return this.get();
		}

	}

	@FunctionalInterface
	public interface ContextDependentSupplierFunction<Context, Type>
		extends Supplier<Type>, Function<Context, Type> {

		@Override
		default Type get() {
			return this.apply(null);
		}

	}

}
