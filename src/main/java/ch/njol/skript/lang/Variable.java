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
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.structures.StructVariables.DefaultVariables;
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
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.OperationInfo;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.script.ScriptWarning;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.function.Function;

public class Variable<T> implements Expression<T> {

	private final static String SINGLE_SEPARATOR_CHAR = ":";
	public final static String SEPARATOR = SINGLE_SEPARATOR_CHAR + SINGLE_SEPARATOR_CHAR;
	public final static String LOCAL_VARIABLE_TOKEN = "_";

	/**
	 * Script this variable was created in.
	 */
	private final @Nullable Script script;

	/**
	 * The name of this variable, excluding the local variable token, but including the list variable token '::*'.
	 */
	private final VariableString name;

	private final Class<T> superType;
	private final Class<? extends T>[] types;

	private final boolean local;
	private final boolean list;

	private final @Nullable Variable<?> source;

	@SuppressWarnings("unchecked")
	private Variable(VariableString name, Class<? extends T>[] types, boolean local, boolean list, @Nullable Variable<?> source) {
		assert types.length > 0;

		assert name.isSimple() || name.getMode() == StringMode.VARIABLE_NAME;

		ParserInstance parser = getParser();

		this.script = parser.isActive() ? parser.getCurrentScript() : null;

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
				char character = name.charAt(i);
				if (character == '*')
					asterisks.add(i);
				else if (character == '%')
					percents.add(i);
			}
			int count = asterisks.size();
			int index = 0;
			for (int i = 0; i < percents.size(); i += 2) {
				if (index == asterisks.size() || i+1 == percents.size()) // Out of bounds
					break;
				int lowerBound = percents.get(i), upperBound = percents.get(i+1);
				// Continually decrement asterisk count by checking if any asterisks in current range
				while (index < asterisks.size() && lowerBound < asterisks.get(index) && asterisks.get(index) < upperBound) {
					count--;
					index++;
				}
			}
			if (!(count == 0 || (count == 1 && name.endsWith(SEPARATOR + "*")))) {
				if (printErrors) {
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
	public static <T> @Nullable Variable<T> newInstance(String name, Class<? extends T>[] types) {
		name = "" + name.trim();
		if (!isValidVariableName(name, true, true))
			return null;
		VariableString variableString = VariableString.newInstance(
			name.startsWith(LOCAL_VARIABLE_TOKEN) ? name.substring(LOCAL_VARIABLE_TOKEN.length()).trim() : name, StringMode.VARIABLE_NAME);
		if (variableString == null)
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
				"{" + StringUtils.substring(currentScript.getConfig().getFileName(), 0, -3) + SEPARATOR + name + "}");
		}

		// Check for local variable type hints
		if (isLocal && variableString.isSimple()) { // Only variable names we fully know already
			Class<?> hint = TypeHints.get(variableString.toString());
			if (hint != null && !hint.equals(Object.class)) { // Type hint available
				// See if we can get correct type without conversion
				for (Class<? extends T> type : types) {
					assert type != null;
					if (type.isAssignableFrom(hint)) {
						// Hint matches, use variable with exactly correct type
						return new Variable<>(variableString, CollectionUtils.array(type), true, isPlural, null);
					}
				}

				// Or with conversion?
				for (Class<? extends T> type : types) {
					if (Converters.converterExists(hint, type)) {
						// Hint matches, even though converter is needed
						return new Variable<>(variableString, CollectionUtils.array(type), true, isPlural, null);
					}

					// Special cases
					if (type.isAssignableFrom(World.class) && hint.isAssignableFrom(String.class)) {
						// String->World conversion is weird spaghetti code
						return new Variable<>(variableString, types, true, isPlural, null);
					} else if (type.isAssignableFrom(Player.class) && hint.isAssignableFrom(String.class)) {
						// String->Player conversion is not available at this point
						return new Variable<>(variableString, types, true, isPlural, null);
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

		return new Variable<>(variableString, types, isLocal, isPlural, null);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
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
	public String toString(@Nullable Event event, boolean debug) {
		StringBuilder stringBuilder = new StringBuilder()
			.append("{");
		if (local)
			stringBuilder.append(LOCAL_VARIABLE_TOKEN);
		stringBuilder.append(StringUtils.substring(name.toString(event, debug), 1, -1))
			.append("}");

		if (debug) {
			stringBuilder.append(" (");
			if (event != null) {
				stringBuilder.append(Classes.toString(get(event)))
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
	@SuppressWarnings("unchecked")
	public <R> Variable<R> getConvertedExpression(Class<R>... to) {
		return new Variable<>(name, to, local, list, this);
	}

	/**
	 * Gets the value of this variable as stored in the variables map.
	 * This method also checks against default variables.
	 */
	public @Nullable Object getRaw(Event event) {
		DefaultVariables data = script == null ? null : script.getData(DefaultVariables.class);
		if (data != null)
			data.enterScope();
		try {
			String name = this.name.toString(event);

			// prevents e.g. {%expr%} where "%expr%" ends with "::*" from returning a Map
			if (name.endsWith(Variable.SEPARATOR + "*") != list)
				return null;
			Object value = !list ? convertIfOldPlayer(name, local, event, Variables.getVariable(name, event, local)) : Variables.getVariable(name, event, local);
			if (value != null)
				return value;

			// Check for default variables if value is still null.
			if (data == null || !data.hasDefaultVariables())
				return null;

			for (String typeHint : this.name.getDefaultVariableNames(name, event)) {
				value = Variables.getVariable(typeHint, event, false);
				if (value != null)
					return value;
			}
		} finally {
			if (data != null)
				data.exitScope();
		}
		return null;
	}

	private @Nullable Object get(Event event) {
		Object rawValue = getRaw(event);
		if (!list)
			return rawValue;
		if (rawValue == null)
			return Array.newInstance(types[0], 0);
		List<Object> convertedValues = new ArrayList<>();
		String name = StringUtils.substring(this.name.toString(event), 0, -1);
		//noinspection unchecked
		for (Entry<String, ?> variable : ((Map<String, ?>) rawValue).entrySet()) {
			if (variable.getKey() != null && variable.getValue() != null) {
				Object value;
				if (variable.getValue() instanceof Map)
					//noinspection unchecked
					value = ((Map<String, ?>) variable.getValue()).get(null);
				else
					value = variable.getValue();
				if (value != null)
					convertedValues.add(convertIfOldPlayer(name + variable.getKey(), local, event, value));
			}
		}
		return convertedValues.toArray();
	}

	/*
	 * Workaround for player variables when a player has left and rejoined
	 * because the player object inside the variable will be a (kinda) dead variable
	 * as a new player object has been created by the server.
	 */
	public static <T> @Nullable T convertIfOldPlayer(String key, boolean local, Event event, @Nullable T object) {
		if (SkriptConfig.enablePlayerVariableFix.value() && object instanceof Player oldPlayer) {
			if (!oldPlayer.isValid() && oldPlayer.isOnline()) {
				Player newPlayer = Bukkit.getPlayer(oldPlayer.getUniqueId());
				Variables.setVariable(key, newPlayer, event, local);
				//noinspection unchecked
				return (T) newPlayer;
			}
		}
		return object;
	}

	public Iterator<Pair<String, Object>> variablesIterator(Event event) {
		if (!list)
			throw new SkriptAPIException("Looping a non-list variable");
		return Variables.getVariableIterator(name.toString(event), local, event);
	}

	@Override
	public @Nullable Iterator<T> iterator(Event event) {
		if (!list) {
			T value = getSingle(event);
			return value != null ? new SingleItemIterator<>(value) : null;
		}
		String name = StringUtils.substring(this.name.toString(event), 0, -1);
		Object value = Variables.getVariable(name + "*", event, local);
		if (value == null)
			return new EmptyIterator<>();
		assert value instanceof TreeMap;
		// temporary list to prevent CMEs
		//noinspection unchecked
		Iterator<String> keys = new ArrayList<>(((Map<String, Object>) value).keySet()).iterator();
		return new Iterator<>() {
			private @Nullable T next = null;

			@Override
			public boolean hasNext() {
				if (next != null)
					return true;
				while (keys.hasNext()) {
					@Nullable String key = keys.next();
					if (key != null) {
						next = Converters.convert(Variables.getVariable(name + key, event, local), types);

						//noinspection unchecked
						next = (T) convertIfOldPlayer(name + key, local, event, next);
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

	private @Nullable T getConverted(Event event) {
		assert !list;
		return Converters.convert(get(event), types);
	}

	private T[] getConvertedArray(Event event) {
		assert list;
		return Converters.convert((Object[]) get(event), types, superType);
	}

	private void set(Event event, @Nullable Object value) {
		Variables.setVariable("" + name.toString(event), value, event, local);
	}

	private void setIndex(Event event, String index, @Nullable Object value) {
		assert list;
		String name = this.name.toString(event);
		assert name.endsWith(SEPARATOR + "*") : name + "; " + this.name;
		Variables.setVariable(name.substring(0, name.length() - 1) + index, value, event, local);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!list && mode == ChangeMode.SET)
			return CollectionUtils.array(Object.class);
		return CollectionUtils.array(Object[].class);
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) throws UnsupportedOperationException {
		switch (mode) {
			case DELETE:
				if (list) {
					ArrayList<String> toDelete = new ArrayList<>();
					Map<String, Object> map = (Map<String, Object>) getRaw(event);
					if (map == null)
						return;
					for (Entry<String, Object> entry : map.entrySet()) {
						if (entry.getKey() != null){
							toDelete.add(entry.getKey());
						}
					}
					for (String index : toDelete) {
						assert index != null;
						setIndex(event, index, null);
					}
				}

				set(event, null);
				break;
			case SET:
				assert delta != null;
				if (list) {
					set(event, null);
					int i = 1;
					for (Object value : delta) {
						if (value instanceof Object[]) {
							for (int j = 0; j < ((Object[]) value).length; j++) {
								setIndex(event, "" + i + SEPARATOR + (j + 1), ((Object[]) value)[j]);
							}
						} else {
							setIndex(event, "" + i, value);
						}
						i++;
					}
				} else if (delta.length > 0) {
					// if length = 0, likely a failure in casting
					// (eg, set vector length of {_notvector} to 1, which casts delta to Vector[], resulting in an empty Vector array)
					// so just do nothing.
					set(event, delta[0]);
				}
				break;
			case RESET:
				Object rawValue = getRaw(event);
				if (rawValue == null)
					return;
				for (Object values : rawValue instanceof Map ? ((Map<?, ?>) rawValue).values() : Arrays.asList(rawValue)) {
					Class<?> type = values.getClass();
					assert type != null;
					ClassInfo<?> classInfo = Classes.getSuperClassInfo(type);
					Changer<?> changer = classInfo.getChanger();
					if (changer != null && changer.acceptChange(ChangeMode.RESET) != null) {
						Object[] valueArray = (Object[]) Array.newInstance(values.getClass(), 1);
						valueArray[0] = values;
						((Changer) changer).change(valueArray, null, ChangeMode.RESET);
					}
				}
				break;
			case ADD:
			case REMOVE:
			case REMOVE_ALL:
				assert delta != null;
				if (list) {
					Map<String, Object> map = (Map<String, Object>) getRaw(event);
					if (mode == ChangeMode.REMOVE) {
						if (map == null)
							return;
						ArrayList<String> toRemove = new ArrayList<>(); // prevents CMEs
						for (Object value : delta) {
							for (Entry<String, Object> entry : map.entrySet()) {
								if (Relation.EQUAL.isImpliedBy(Comparators.compare(entry.getValue(), value))) {
									String key = entry.getKey();
									if (key == null)
										continue; // This is NOT a part of list variable

									// Otherwise, we'll mark that key to be set to null
									toRemove.add(key);
									break;
								}
							}
						}
						for (String index : toRemove) {
							assert index != null;
							setIndex(event, index, null);
						}
					} else if (mode == ChangeMode.REMOVE_ALL) {
						if (map == null)
							return;
						ArrayList<String> toRemove = new ArrayList<>(); // prevents CMEs
						for (Entry<String, Object> i : map.entrySet()) {
							for (Object value : delta) {
								if (Relation.EQUAL.isImpliedBy(Comparators.compare(i.getValue(), value)))
									toRemove.add(i.getKey());
							}
						}
						for (String index : toRemove) {
							assert index != null;
							setIndex(event, index, null);
						}
					} else {
						assert mode == ChangeMode.ADD;
						int i = 1;
						for (Object value : delta) {
							if (map != null)
								while (map.containsKey("" + i))
									i++;
							setIndex(event, "" + i, value);
							i++;
						}
					}
				} else {
					Object originalValue = get(event);
					Class<?> clazz = originalValue == null ? null : originalValue.getClass();
					Operator operator = mode == ChangeMode.ADD ? Operator.ADDITION : Operator.SUBTRACTION;
					Changer<?> changer;
					Class<?>[] classes;
					if (clazz == null || !Arithmetics.getOperations(operator, clazz).isEmpty()) {
						boolean changed = false;
						for (Object newValue : delta) {
							OperationInfo info = Arithmetics.getOperationInfo(operator, clazz != null ? (Class) clazz : newValue.getClass(), newValue.getClass());
							if (info == null)
								continue;

							Object value = originalValue == null ? Arithmetics.getDefaultValue(info.getLeft()) : originalValue;
							if (value == null)
								continue;

							originalValue = info.getOperation().calculate(value, newValue);
							changed = true;
						}
						if (changed)
							set(event, originalValue);
					} else if ((changer = Classes.getSuperClassInfo(clazz).getChanger()) != null && (classes = changer.acceptChange(mode)) != null) {
						Object[] originalValueArray = (Object[]) Array.newInstance(originalValue.getClass(), 1);
						originalValueArray[0] = originalValue;

						Class<?>[] classes2 = new Class<?>[classes.length];
						for (int i = 0; i < classes.length; i++)
							classes2[i] = classes[i].isArray() ? classes[i].getComponentType() : classes[i];

						ArrayList<Object> convertedDelta = new ArrayList<>();
						for (Object value : delta) {
							Object convertedValue = Converters.convert(value, classes2);
							if (convertedValue != null)
								convertedDelta.add(convertedValue);
						}

						ChangerUtils.change(changer, originalValueArray, convertedDelta.toArray(), mode);

					}
				}
				break;
		}
	}

	@Override
	public <R> void changeInPlace(Event event, Function<T, R> changeFunction) {
		if (!list) {
			T value = getSingle(event);
			if (value == null)
				return;
			set(event, changeFunction.apply(value));
			return;
		}
		variablesIterator(event).forEachRemaining(pair -> {
			String index = pair.getKey();
			T value = Converters.convert(pair.getValue(), types);
			if (value == null)
				return;
			Object newValue = changeFunction.apply(value);
			setIndex(event, index, newValue);
		});
	}

	@Override
	public @Nullable T getSingle(Event event) {
		if (list)
			throw new SkriptAPIException("Invalid call to getSingle");
		return getConverted(event);
	}

	@Override
	public T[] getArray(Event event) {
		return getAll(event);
	}

	@Override
	@SuppressWarnings("unchecked")
	public T[] getAll(Event event) {
		if (list)
			return getConvertedArray(event);
		T value = getConverted(event);
		if (value == null) {
			return (T[]) Array.newInstance(superType, 0);
		}
		T[] valueArray = (T[]) Array.newInstance(superType, 1);
		valueArray[0] = value;
		return valueArray;
	}

	@Override
	public boolean isLoopOf(String input) {
		return input.equalsIgnoreCase("var") || input.equalsIgnoreCase("variable") || input.equalsIgnoreCase("value") || input.equalsIgnoreCase("index");
	}

	public boolean isIndexLoop(String input) {
		return input.equalsIgnoreCase("index");
	}

	@Override
	public boolean check(Event event, Checker<? super T> checker, boolean negated) {
		return SimpleExpression.check(getAll(event), checker, negated, getAnd());
	}

	@Override
	public boolean check(Event event, Checker<? super T> checker) {
		return SimpleExpression.check(getAll(event), checker, false, getAnd());
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
		Variable<?> source = this.source;
		return source == null ? this : source;
	}

	@Override
	public Expression<? extends T> simplify() {
		return this;
	}

	@Override
	public boolean supportsLoopPeeking() {
		return true;
	}

}
