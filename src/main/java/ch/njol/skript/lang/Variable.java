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

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.Arithmetic;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.classes.ClassInfo;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.script.ScriptWarning;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.converter.Converters;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.TypeHints;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.EmptyIterator;
import ch.njol.util.coll.iterator.SingleItemIterator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.TreeMap;

/**
 * @author Peter Güttinger
 */
public class Variable<T> implements Expression<T> {

	private final static String SINGLE_SEPARATOR_CHAR = ":";
	public final static String SEPARATOR = SINGLE_SEPARATOR_CHAR + SINGLE_SEPARATOR_CHAR;
	public final static String LOCAL_VARIABLE_TOKEN = "_";

	/**
	 * The name of this variable, excluding the local variable token, but including the list variable token '::*'.
	 */
	private final VariableString name;

	private final Class<T> superType;
	final Class<? extends T>[] types;

	final boolean local;
	private final boolean list;

	@Nullable
	private final Variable<?> source;

	@SuppressWarnings("unchecked")
	private Variable(VariableString name, Class<? extends T>[] types, boolean local, boolean list, @Nullable Variable<?> source) {
		assert name != null;
		assert types != null && types.length > 0;

		assert name.isSimple() || name.getMode() == StringMode.VARIABLE_NAME;

		this.local = local;
		this.list = list;

		this.name = name;

		this.types = types;
		this.superType = (Class<T>) Utils.getSuperType(types);

		this.source = source;
	}

	/**
	 * Checks whether a string is a valid variable name. This is used to verify variable names as well as command and function arguments.
	 *
	 * @param name The name to test
	 * @param allowListVariable Whether to allow a list variable
	 * @param printErrors Whether to print errors when they are encountered
	 * @return true if the name is valid, false otherwise.
	 */
	public static boolean isValidVariableName(String name, boolean allowListVariable, boolean printErrors) {
		name = name.startsWith(LOCAL_VARIABLE_TOKEN) ? "" + name.substring(LOCAL_VARIABLE_TOKEN.length()).trim() : "" + name.trim();
		if (!allowListVariable && name.contains(SEPARATOR)) {
			if (printErrors)
				Skript.error("List variables are not allowed here (error in variable {" + name + "})");
			return false;
		} else if (name.startsWith(SEPARATOR) || name.endsWith(SEPARATOR)) {
			if (printErrors)
				Skript.error("A variable's name must neither start nor end with the separator '" + SEPARATOR + "' (error in variable {" + name + "})");
			return false;
		} else if (name.contains("*") && (!allowListVariable || name.indexOf("*") != name.length() - 1 || !name.endsWith(SEPARATOR + "*"))) {
			List<Integer> asterisks = new ArrayList<>();
			List<Integer> percents = new ArrayList<>();
			for (int i = 0; i < name.length(); i++) {
				char c = name.charAt(i);
				if (c == '*')
					asterisks.add(i);
				else if (c == '%')
					percents.add(i);
			}
			int count = asterisks.size();
			int index = 0;
			for (int i = 0; i < percents.size(); i += 2) {
				if (index == asterisks.size() || i+1 == percents.size()) // Out of bounds 
					break;
				int lb = percents.get(i), ub = percents.get(i+1);
				// Continually decrement asterisk count by checking if any asterisks in current range 
				while (index < asterisks.size() && lb < asterisks.get(index) && asterisks.get(index) < ub) {
					count--;
					index++;
				}
			}
			if (!(count == 0 || (count == 1 && name.endsWith(SEPARATOR + "*")))) {
				if (printErrors) {
					if (name.indexOf("*") == 0)
						Skript.error("[2.0] Local variables now start with an underscore, e.g. {_local variable}. The asterisk is reserved for list variables. (error in variable {" + name + "})");
					else
						Skript.error("A variable's name must not contain any asterisks except at the end after '" + SEPARATOR + "' to denote a list variable, e.g. {variable" + SEPARATOR + "*} (error in variable {" + name + "})");
				}
				return false;
			}
		} else if (name.contains(SEPARATOR + SEPARATOR)) {
			if (printErrors)
				Skript.error("A variable's name must not contain the separator '" + SEPARATOR + "' multiple times in a row (error in variable {" + name + "})");
			return false;
		} else if (name.replace(SEPARATOR, "").contains(SINGLE_SEPARATOR_CHAR)) {
			if (printErrors)
				Skript.warning("If you meant to make the variable {" + name + "} a list, its name should contain '"
					+ SEPARATOR + "'. Having a single '" + SINGLE_SEPARATOR_CHAR + "' does nothing!");
		}
		return true;
	}

	/**
	 * Prints errors
	 */
	@Nullable
	public static <T> Variable<T> newInstance(String name, Class<? extends T>[] types) {
		name = "" + name.trim();
		if (!isValidVariableName(name, true, true))
			return null;
		VariableString vs = VariableString.newInstance(
			name.startsWith(LOCAL_VARIABLE_TOKEN) ? name.substring(LOCAL_VARIABLE_TOKEN.length()).trim() : name, StringMode.VARIABLE_NAME);
		if (vs == null)
			return null;

		boolean isLocal = name.startsWith(LOCAL_VARIABLE_TOKEN);
		boolean isPlural = name.endsWith(SEPARATOR + "*");

		ParserInstance parser = ParserInstance.get();
		Script currentScript = parser.isActive() ? parser.getCurrentScript() : null;
		if (currentScript != null
				&& !SkriptConfig.disableVariableStartingWithExpressionWarnings.value()
				&& !currentScript.suppressesWarning(ScriptWarning.VARIABLE_STARTS_WITH_EXPRESSION)
				&& (isLocal ? name.substring(LOCAL_VARIABLE_TOKEN.length()) : name).startsWith("%")) {
			Skript.warning("Starting a variable's name with an expression is discouraged ({" + name + "}). " +
				"You could prefix it with the script's name: " +
				"{" + StringUtils.substring(currentScript.getConfig().getFileName(), 0, -3) + "." + name + "}");
		}

		// Check for local variable type hints
		if (isLocal && vs.isSimple()) { // Only variable names we fully know already
			Class<?> hint = TypeHints.get(vs.toString());
			if (hint != null && !hint.equals(Object.class)) { // Type hint available
				// See if we can get correct type without conversion
				for (Class<? extends T> type : types) {
					assert type != null;
					if (type.isAssignableFrom(hint)) {
						// Hint matches, use variable with exactly correct type
						return new Variable<>(vs, CollectionUtils.array(type), isLocal, isPlural, null);
					}
				}

				// Or with conversion?
				for (Class<? extends T> type : types) {
					if (Converters.converterExists(hint, type)) {
						// Hint matches, even though converter is needed
						return new Variable<>(vs, CollectionUtils.array(type), isLocal, isPlural, null);
					}

					// Special cases
					if (type.isAssignableFrom(World.class) && hint.isAssignableFrom(String.class)) {
						// String->World conversion is weird spaghetti code
						return new Variable<>(vs, types, isLocal, isPlural, null);
					} else if (type.isAssignableFrom(Player.class) && hint.isAssignableFrom(String.class)) {
						// String->Player conversion is not available at this point
						return new Variable<>(vs, types, isLocal, isPlural, null);
					}
				}

				// Hint exists and does NOT match any types requested
				ClassInfo<?>[] infos = new ClassInfo[types.length];
				for (int i = 0; i < types.length; i++) {
					infos[i] = Classes.getExactClassInfo(types[i]);
				}
				Skript.warning("Variable '{_" + name + "}' is " + Classes.toString(Classes.getExactClassInfo(hint))
					+ ", not " + Classes.toString(infos, false));
				// Fall back to not having any type hints
			}
		}

		return new Variable<>(vs, types, isLocal, isPlural, null);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}

	public boolean isLocal() {
		return local;
	}

	public boolean isList() {
		return list;
	}

	@Override
	public boolean isSingle() {
		return !list;
	}

	@Override
	public Class<? extends T> getReturnType() {
		return superType;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		StringBuilder stringBuilder = new StringBuilder()
			.append("{");
		if (local)
			stringBuilder.append(LOCAL_VARIABLE_TOKEN);
		stringBuilder.append(StringUtils.substring(name.toString(e, debug), 1, -1))
			.append("}");

		if (debug) {
			stringBuilder.append(" (");
			if (e != null) {
				stringBuilder.append(Classes.toString(get(e)))
					.append(", ");
			}
			stringBuilder.append("as ")
				.append(superType.getName())
				.append(")");
		}
		return stringBuilder.toString();
	}

	@Override
	public String toString() {
		return toString(null, false);
	}

	@Override
	public <R> Variable<R> getConvertedExpression(Class<R>... to) {
		return new Variable<>(name, to, local, list, this);
	}

	/**
	 * Gets the value of this variable as stored in the variables map.
	 */
	@Nullable
	public Object getRaw(Event e) {
		String n = name.toString(e);
		if (n.endsWith(Variable.SEPARATOR + "*") != list) // prevents e.g. {%expr%} where "%expr%" ends with "::*" from returning a Map
			return null;
		Object val = !list ? convertIfOldPlayer(n, e, Variables.getVariable(n, e, local)) : Variables.getVariable(n, e, local);
		if (val == null)
			return Variables.getVariable((local ? LOCAL_VARIABLE_TOKEN : "") + name.getDefaultVariableName(), e, false);
		return val;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private Object get(Event e) {
		Object val = getRaw(e);
		if (!list)
			return val;
		if (val == null)
			return Array.newInstance(types[0], 0);
		List<Object> l = new ArrayList<>();
		String name = StringUtils.substring(this.name.toString(e), 0, -1);
		for (Entry<String, ?> v : ((Map<String, ?>) val).entrySet()) {
			if (v.getKey() != null && v.getValue() != null) {
				Object o;
				if (v.getValue() instanceof Map)
					o = ((Map<String, ?>) v.getValue()).get(null);
				else
					o = v.getValue();
				if (o != null)
					l.add(convertIfOldPlayer(name + v.getKey(), e, o));
			}
		}
		return l.toArray();
	}

	/*
	 * Workaround for player variables when a player has left and rejoined
	 * because the player object inside the variable will be a (kinda) dead variable
	 * as a new player object has been created by the server.
	 */
	@Nullable Object convertIfOldPlayer(String key, Event event, @Nullable Object t){
		if(SkriptConfig.enablePlayerVariableFix.value() && t != null && t instanceof Player){
			Player p = (Player) t;
			if(!p.isValid() && p.isOnline()){
				Player player = Bukkit.getPlayer(p.getUniqueId());
				Variables.setVariable(key, player, event, local);
				return player;
			}
		}
		return t;
	}

	public Iterator<Pair<String, Object>> variablesIterator(Event e) {
		if (!list)
			throw new SkriptAPIException("Looping a non-list variable");
		String name = StringUtils.substring(this.name.toString(e), 0, -1);
		Object val = Variables.getVariable(name + "*", e, local);
		if (val == null)
			return new EmptyIterator<>();
		assert val instanceof TreeMap;
		// temporary list to prevent CMEs
		@SuppressWarnings("unchecked")
		Iterator<String> keys = new ArrayList<>(((Map<String, Object>) val).keySet()).iterator();
		return new Iterator<Pair<String, Object>>() {
			@Nullable
			private String key;
			@Nullable
			private Object next = null;

			@Override
			public boolean hasNext() {
				if (next != null)
					return true;
				while (keys.hasNext()) {
					key = keys.next();
					if (key != null) {
						next = convertIfOldPlayer(name + key, e, Variables.getVariable(name + key, e, local));
						if (next != null && !(next instanceof TreeMap))
							return true;
					}
				}
				next = null;
				return false;
			}

			@Override
			public Pair<String, Object> next() {
				if (!hasNext())
					throw new NoSuchElementException();
				Pair<String, Object> n = new Pair<>(key, next);
				next = null;
				return n;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	@Nullable
	public Iterator<T> iterator(Event e) {
		if (!list) {
			T item = getSingle(e);
			return item != null ? new SingleItemIterator<>(item) : null;
		}
		String name = StringUtils.substring(this.name.toString(e), 0, -1);
		Object val = Variables.getVariable(name + "*", e, local);
		if (val == null)
			return new EmptyIterator<>();
		assert val instanceof TreeMap;
		// temporary list to prevent CMEs
		@SuppressWarnings("unchecked")
		Iterator<String> keys = new ArrayList<>(((Map<String, Object>) val).keySet()).iterator();
		return new Iterator<T>() {
			@Nullable
			private String key;
			@Nullable
			private T next = null;

			@SuppressWarnings({"unchecked"})
			@Override
			public boolean hasNext() {
				if (next != null)
					return true;
				while (keys.hasNext()) {
					key = keys.next();
					if (key != null) {
						next = Converters.convert(Variables.getVariable(name + key, e, local), types);
						next = (T) convertIfOldPlayer(name + key, e, next);
						if (next != null && !(next instanceof TreeMap))
							return true;
					}
				}
				next = null;
				return false;
			}

			@Override
			public T next() {
				if (!hasNext())
					throw new NoSuchElementException();
				T n = next;
				assert n != null;
				next = null;
				return n;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Nullable
	private T getConverted(Event e) {
		assert !list;
		return Converters.convert(get(e), types);
	}

	private T[] getConvertedArray(Event e) {
		assert list;
		return Converters.convert((Object[]) get(e), types, superType);
	}

	private void set(Event e, @Nullable Object value) {
		Variables.setVariable("" + name.toString(e), value, e, local);
	}

	private void setIndex(Event e, String index, @Nullable Object value) {
		assert list;
		String s = name.toString(e);
		assert s.endsWith("::*") : s + "; " + name;
		Variables.setVariable(s.substring(0, s.length() - 1) + index, value, e, local);
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (!list && mode == ChangeMode.SET)
			return CollectionUtils.array(Object.class);
		return CollectionUtils.array(Object[].class);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) throws UnsupportedOperationException {
		switch (mode) {
			case DELETE:
				if (list) {
					ArrayList<String> rem = new ArrayList<>();
					Map<String, Object> o = (Map<String, Object>) getRaw(e);
					if (o == null)
						return;
					for (Entry<String, Object> i : o.entrySet()) {
						if (i.getKey() != null){
							rem.add(i.getKey());
						}
					}
					for (String r : rem) {
						assert r != null;
						setIndex(e, r, null);
					}
				}

				set(e, null);
				break;
			case SET:
				assert delta != null;
				if (list) {
					set(e, null);
					int i = 1;
					for (Object d : delta) {
						if (d instanceof Object[]) {
							for (int j = 0; j < ((Object[]) d).length; j++) {
								setIndex(e, "" + i + SEPARATOR + j, ((Object[]) d)[j]);
							}
						} else {
							setIndex(e, "" + i, d);
						}
						i++;
					}
				} else {
					set(e, delta[0]);
				}
				break;
			case RESET:
				Object x = getRaw(e);
				if (x == null)
					return;
				for (Object o : x instanceof Map ? ((Map<?, ?>) x).values() : Arrays.asList(x)) {
					Class<?> c = o.getClass();
					assert c != null;
					ClassInfo<?> ci = Classes.getSuperClassInfo(c);
					Changer<?> changer = ci.getChanger();
					if (changer != null && changer.acceptChange(ChangeMode.RESET) != null) {
						Object[] one = (Object[]) Array.newInstance(o.getClass(), 1);
						one[0] = o;
						((Changer) changer).change(one, null, ChangeMode.RESET);
					}
				}
				break;
			case ADD:
			case REMOVE:
			case REMOVE_ALL:
				assert delta != null;
				if (list) {
					Map<String, Object> o = (Map<String, Object>) getRaw(e);
					if (mode == ChangeMode.REMOVE) {
						if (o == null)
							return;
						ArrayList<String> rem = new ArrayList<>(); // prevents CMEs
						for (Object d : delta) {
							for (Entry<String, Object> i : o.entrySet()) {
								if (Relation.EQUAL.isImpliedBy(Comparators.compare(i.getValue(), d))) {
									String key = i.getKey();
									if (key == null)
										continue; // This is NOT a part of list variable

									// Otherwise, we'll mark that key to be set to null
									rem.add(key);
									break;
								}
							}
						}
						for (String r : rem) {
							assert r != null;
							setIndex(e, r, null);
						}
					} else if (mode == ChangeMode.REMOVE_ALL) {
						if (o == null)
							return;
						ArrayList<String> rem = new ArrayList<>(); // prevents CMEs
						for (Entry<String, Object> i : o.entrySet()) {
							for (Object d : delta) {
								if (Relation.EQUAL.isImpliedBy(Comparators.compare(i.getValue(), d)))
									rem.add(i.getKey());
							}
						}
						for (String r : rem) {
							assert r != null;
							setIndex(e, r, null);
						}
					} else {
						assert mode == ChangeMode.ADD;
						int i = 1;
						for (Object d : delta) {
							if (o != null)
								while (o.containsKey("" + i))
									i++;
							setIndex(e, "" + i, d);
							i++;
						}
					}
				} else {
					Object o = get(e);
					ClassInfo<?> ci;
					if (o == null) {
						ci = null;
					} else {
						Class<?> c = o.getClass();
						assert c != null;
						ci = Classes.getSuperClassInfo(c);
					}
					Arithmetic a = null;
					Changer<?> changer;
					Class<?>[] cs;
					if (o == null || ci == null || (a = ci.getMath()) != null) {
						boolean changed = false;
						for (Object d : delta) {
							if (o == null || ci == null) {
								Class<?> c = d.getClass();
								assert c != null;
								ci = Classes.getSuperClassInfo(c);

								if ((a = ci.getMath()) != null)
									o = d;
								if (d instanceof Number) { // Nonexistent variable: add/subtract
									if (mode == ChangeMode.REMOVE) // Variable is delta negated
										o = -((Number) d).doubleValue(); // Hopefully enough precision
									else // Variable is now what was added to it
										o = d;
								}
								changed = true;
								continue;
							}
							Class<?> r = ci.getMathRelativeType();
							assert a != null && r != null : ci;
							Object diff = Converters.convert(d, r);
							if (diff != null) {
								if (mode == ChangeMode.ADD)
									o = a.add(o, diff);
								else
									o = a.subtract(o, diff);
								changed = true;
							}
						}
						if (changed)
							set(e, o);
					} else if ((changer = ci.getChanger()) != null && (cs = changer.acceptChange(mode)) != null) {
						Object[] one = (Object[]) Array.newInstance(o.getClass(), 1);
						one[0] = o;

						Class<?>[] cs2 = new Class<?>[cs.length];
						for (int i = 0; i < cs.length; i++)
							cs2[i] = cs[i].isArray() ? cs[i].getComponentType() : cs[i];

						ArrayList<Object> l = new ArrayList<>();
						for (Object d : delta) {
							Object d2 = Converters.convert(d, cs2);
							if (d2 != null)
								l.add(d2);
						}

						ChangerUtils.change(changer, one, l.toArray(), mode);

					}
				}
				break;
		}
	}

	@Override
	@Nullable
	public T getSingle(Event e) {
		if (list)
			throw new SkriptAPIException("Invalid call to getSingle");
		return getConverted(e);
	}

	@Override
	public T[] getArray(Event e) {
		return getAll(e);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T[] getAll(Event e) {
		if(list)
			return getConvertedArray(e);
		T o = getConverted(e);
		if (o == null) {
			T[] r = (T[]) Array.newInstance(superType, 0);
			assert r != null;
			return r;
		}
		T[] one = (T[]) Array.newInstance(superType, 1);
		one[0] = o;
		return one;
	}

	@Override
	public boolean isLoopOf(String s) {
		return s.equalsIgnoreCase("var") || s.equalsIgnoreCase("variable") || s.equalsIgnoreCase("value") || s.equalsIgnoreCase("index");
	}

	public boolean isIndexLoop(String s) {
		return s.equalsIgnoreCase("index");
	}

	@Override
	public boolean check(Event e, Checker<? super T> c, boolean negated) {
		return SimpleExpression.check(getAll(e), c, negated, getAnd());
	}

	@Override
	public boolean check(Event e, Checker<? super T> c) {
		return SimpleExpression.check(getAll(e), c, false, getAnd());
	}

	public VariableString getName() {
		return name;
	}

	@Override
	public boolean getAnd() {
		return true;
	}

	@Override
	public boolean setTime(int time) {
		return false;
	}

	@Override
	public int getTime() {
		return 0;
	}

	@Override
	public boolean isDefault() {
		return false;
	}

	@Override
	public Expression<?> getSource() {
		Variable<?> s = source;
		return s == null ? this : s;
	}

	@Override
	public Expression<? extends T> simplify() {
		return this;
	}

}
