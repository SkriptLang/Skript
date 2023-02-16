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

import java.util.logging.Level;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

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
		
		// V2
//		if (to[0] != Object.class) {
//			return (Literal<? extends R>) SkriptParser.parseExpression(data, new Converter<String, Literal<? extends R>>() {
//				@Override
//				public Literal<? extends R> convert(final String s) {
//					for (final Class<? extends R> c : to) {
//						final R r = Classes.parse(s, c, context);
//						if (r != null)
//							return new SimpleLiteral<R>(r, false);
//					}
//					return null;
//				}
//			}, "'" + data + "' is " + SkriptParser.notOfType(to));
//		}
//		return (Literal<? extends R>) SkriptParser.parseExpression(data, new Converter<String, Literal<Object>>() {
//			@Override
//			public Literal<Object> convert(final String s) {
//				for (final ClassInfo<?> ci : Classes.getClassInfos()) {
//					if (ci.getParser() != null && ci.getParser().canParse(context)) {
//						final Object o = ci.getParser().parse(s, context);
//						if (o != null)
//							return new SimpleLiteral<Object>(o, false);
//					}
//				}
//				return null;
//			}
//		}, null);
		
		// V1
//		if (to == String.class && context == ParseContext.DEFAULT) {
//			return (Literal<? extends R>) VariableStringLiteral.newInstance(this);
//		} else if (to == Object.class) {
//			final SimpleLog log = SkriptLogger.startSubLog();
//			if (context == ParseContext.DEFAULT) {
//				final VariableStringLiteral vsl = VariableStringLiteral.newInstance(this);
//				if (vsl != null)
//					return (Literal<? extends R>) vsl;
//				if (log.hasErrors()) {
//					log.printLog();
//					return null;
//				}
//			}
//			for (final ClassInfo<?> ci : Classes.getClassInfos()) {
//				if (ci.getParser() != null && ci.getParser().canParse(context)) {
//					log.clear();
//					final Literal<?> l = convert(ci.getC(), ci.getParser(), context);
//					if (l != null) {
//						log.stop();
//						log.printLog();
//						return (Literal<? extends R>) l;
//					}
//				}
//			}
//			log.stop();
//			return null;
//		}
//		final Parser<? extends R> p = Classes.getParser(to);
//		if (p == null || !p.canParse(context))
//			return null;
//		return convert(to, p, context);
	}
	
//	private <T> Literal<T> convert(final Class<T> to, final Parser<?> parser, final ParseContext context) {
//		assert parser.canParse(context);
//		final SimpleLog log = SkriptLogger.startSubLog();
//
//		String last = data;
//		LogEntry lastError = null;
//
//		final T r = (T) parser.parse(data, context);
//		if (r != null) {
//			log.stop();
//			log.printLog();
//			return new SimpleLiteral<T>(r, false);
//		}
//		lastError = log.getFirstError();
//		log.clear();
//
//		final Deque<T> ts = new LinkedList<T>();
//		final Matcher m = SkriptParser.listSplitPattern.matcher(data);
//		int end = data.length();
//		int expectedEnd = -1;
//		boolean and = true;
//		boolean isAndSet = false;
//		while (m.find()) {
//			if (expectedEnd == -1)
//				expectedEnd = m.start();
//			final T t = (T) parser.parse(last = data.substring(m.end(), end), context);
//			lastError = log.getFirstError();
//			if (t != null) {
//				if (!m.group().matches("\\s*,\\s*")) {
//					if (isAndSet) {
//						if (and != m.group().toLowerCase(Locale.ENGLISH).contains("and")) {
//							Skript.warning("list has multiple 'and' or 'or', will default to 'and'");
//							and = true;
//						}
//					} else {
//						and = m.group().toLowerCase(Locale.ENGLISH).contains("and");
//						isAndSet = true;
//					}
//				}
//				ts.addFirst(t);
//				log.clear();
//				end = m.start();
//				m.region(0, end);
//			} else {
//				log.clear();
//			}
//		}
//		if (!isAndSet)
//			Skript.warning("List is missing 'and' or 'or', defaulting to 'and'");
//		if (end == expectedEnd) {
//			final T t = (T) parser.parse(last = data.substring(0, end), context);
//			lastError = log.getFirstError();
//			log.stop();
//			if (t != null) {
//				log.printLog();
//				ts.addFirst(t);
//				return new SimpleLiteral<T>(ts.toArray((T[]) Array.newInstance(to, ts.size())), to, and, this);
//			}
//		}
//		log.stop();
//		if (lastError != null)
//			SkriptLogger.log(lastError);
//		else
//			Skript.error("'" + last + "' is not " + Utils.a(Classes.getSuperClassInfo(to).getName()));
//		return null;
//	}
	
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
	public Expression<? extends Object> simplify() {
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
