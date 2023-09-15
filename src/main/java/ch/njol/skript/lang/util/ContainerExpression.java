/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.lang.util;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Container;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Peter Güttinger
 */
public class ContainerExpression extends SimpleExpression<Object> {

	final Expression<? extends Container<?>> expr;
	private final Class<?> c;

	public ContainerExpression(final Expression<? extends Container<?>> expr, final Class<?> c) {
		this.expr = expr;
		this.c = c;
	}

	@Override
	protected Object[] get(final Event e) {
		throw new UnsupportedOperationException("ContanerExpression must only be used by Loops");
	}

	@Override
	@Nullable
	public Iterator<Object> iterator(final Event e) {
		final Iterator<? extends Container<?>> iter = expr.iterator(e);
		if (iter == null)
			return null;
		return new Iterator<Object>() {
			@Nullable
			private Iterator<?> current;

			@Override
			public boolean hasNext() {
				Iterator<?> c = current;
				while (iter.hasNext() && (c == null || !c.hasNext())) {
					current = c = iter.next().containerIterator();
				}
				return c != null && c.hasNext();
			}

			@Override
			public Object next() {
				if (!hasNext())
					throw new NoSuchElementException();
				final Iterator<?> c = current;
				if (c == null)
					throw new NoSuchElementException();
				final Object o = c.next();
				assert o != null : current + "; " + expr;
				return o;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends Object> getReturnType() {
		return c;
	}

	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return expr.toString(e, debug);
	}

}
