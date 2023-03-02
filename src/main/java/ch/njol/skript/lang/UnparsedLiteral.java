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
package ch.njol.skript.lang;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.NonNullIterator;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.logging.Level;

/**
 * A literal which has yet to be parsed. This is returned if %object(s)% is used within patterns and no expression matches.
 *
 * @author Peter Güttinger
 * @see SimpleLiteral
 */
public class UnparsedLiteral implements Literal<Object> {
	
	private final String data;
	@Nullable
	private final LogEntry error;
	
	/**
	 * @param data non-null, non-empty & trimmed string
	 */
	public UnparsedLiteral(final String data) {
		assert data != null && data.length() > 0;
		this.data = data;
		error = null;
	}
	
	/**
	 * @param data non-null, non-empty & trimmed string
	 * @param error Error to log if this literal cannot be parsed
	 */
	public UnparsedLiteral(final String data, final @Nullable LogEntry error) {
		assert data != null && data.length() > 0;
		assert error == null || error.getLevel() == Level.SEVERE;
		this.data = data;
		this.error = error;
	}
	
	public String getData() {
		return data;
	}
	
	@Override
	public Class<? extends Object> getReturnType() {
		return Object.class;
	}
	
	@Override
	@Nullable
	public <R> Literal<? extends R> getConvertedExpression(final Class<R>... to) {
		return getConvertedExpression(ParseContext.DEFAULT, to);
	}
	
	@Nullable
	public <R> Literal<? extends R> getConvertedExpression(final ParseContext context, final Class<? extends R>... to) {
		assert to != null && to.length > 0;
		assert to.length == 1 || !CollectionUtils.contains(to, Object.class);
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			for (final Class<? extends R> t : to) {
				assert t != null;
				final R r = Classes.parse(data, t, context);
				if (r != null) {
					log.printLog();
					return new SimpleLiteral<>(r, false, this);
				}
				log.clear();
			}
			if (error != null) {
				log.printLog();
				SkriptLogger.log(error);
			} else {
				log.printError();
			}
			return null;
		} finally {
			log.stop();
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "'" + data + "'";
	}
	
	@Override
	public String toString() {
		return toString(null, false);
	}
	
	@Override
	public Expression<?> getSource() {
		return this;
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Expression<?> simplify() {
		return this;
	}
	
	private static SkriptAPIException invalidAccessException() {
		return new SkriptAPIException("UnparsedLiterals must be converted before use");
	}
	
	@Override
	public Object[] getAll() {
		throw invalidAccessException();
	}
	
	@Override
	public Object[] getAll(final Event e) {
		throw invalidAccessException();
	}
	
	@Override
	public Object[] getArray() {
		throw invalidAccessException();
	}
	
	@Override
	public Object[] getArray(final Event e) {
		throw invalidAccessException();
	}
	
	@Override
	public Object getSingle() {
		throw invalidAccessException();
	}
	
	@Override
	public Object getSingle(final Event e) {
		throw invalidAccessException();
	}
	
	@Override
	public NonNullIterator<Object> iterator(final Event e) {
		throw invalidAccessException();
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
		throw invalidAccessException();
	}
	
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		throw invalidAccessException();
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super Object> c) {
		throw invalidAccessException();
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super Object> c, final boolean negated) {
		throw invalidAccessException();
	}
	
	@Override
	public boolean setTime(final int time) {
		throw invalidAccessException();
	}
	
	@Override
	public int getTime() {
		throw invalidAccessException();
	}
	
	@Override
	public boolean isDefault() {
		throw invalidAccessException();
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		throw invalidAccessException();
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		throw invalidAccessException();
	}
	
}
