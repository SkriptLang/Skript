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
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.SingleItemIterator;
import com.google.common.collect.Iterators;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
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
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;

public class Variable<T> implements Expression<T>, KeyReceiverExpression<T>, KeyProviderExpression<T> {

	private final static String SINGLE_SEPARATOR_CHAR = ":";
	public final static String SEPARATOR = SINGLE_SEPARATOR_CHAR + SINGLE_SEPARATOR_CHAR;
	public final static String LOCAL_VARIABLE_TOKEN = "_";
	public static final String EPHEMERAL_VARIABLE_TOKEN = "-";
	private static final char[] reservedTokens = {'~', '.', '+', '$', '!', '&', '^', '*'};

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
	private final boolean ephemeral;
	private final boolean list;

	private final @Nullable Variable<?> source;
	private final Map<Event, String[]> cache = Collections.synchronizedMap(new WeakHashMap<>());

	private ListProvider listProvider = new ShallowListProvider();

	@SuppressWarnings("unchecked")
	private Variable(VariableString name, Class<? extends T>[] types, boolean local, boolean ephemeral, boolean list, @Nullable Variable<?> source) {
		assert types.length > 0;

		assert name.isSimple() || name.getMode() == StringMode.VARIABLE_NAME;

		ParserInstance parser = getParser();

		this.script = parser.isActive() ? parser.getCurrentScript() : null;

		this.local = local;
		this.ephemeral = ephemeral;
		this.list = list;

		this.name = name;

		this.types = types;
		this.superType = (Class<T>) Classes.getSuperClassInfo(types).getC();

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
		assert !name.isEmpty(): "Variable name should not be empty";
		char first = name.charAt(0);
		for (char token : reservedTokens) {
			if (first == token && printErrors) {
				Skript.warning("The character '" + token + "' is reserved at the start of variable names, "
					+ "and may be restricted in future versions");
			}
		}
		name = name.startsWith(LOCAL_VARIABLE_TOKEN) ? name.substring(LOCAL_VARIABLE_TOKEN.length()).trim() : name.trim();
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
	 * Creates a new variable instance with the given name and types. Prints errors.
	 * @param name The raw name of the variable.
	 * @param types The types this variable is expected to be.
	 * @return A new variable instance, or null if the name is invalid or the variable could not be created.
	 * @param <T> The supertype the variable is expected to be.
	 */
	public static <T> @Nullable Variable<T> newInstance(String name, Class<? extends T>[] types) {
		name = name.trim();
		if (!isValidVariableName(name, true, true))
			return null;
		VariableString variableString = VariableString.newInstance(
			name.startsWith(LOCAL_VARIABLE_TOKEN) ? name.substring(LOCAL_VARIABLE_TOKEN.length()).trim() : name, StringMode.VARIABLE_NAME);
		if (variableString == null)
			return null;

		boolean isLocal = name.startsWith(LOCAL_VARIABLE_TOKEN);
		boolean isEphemeral = name.startsWith(EPHEMERAL_VARIABLE_TOKEN);
		boolean isPlural = name.endsWith(SEPARATOR + "*");

		ParserInstance parser = ParserInstance.get();
		Script currentScript = parser.isActive() ? parser.getCurrentScript() : null;

		// check for 'starting with expression' warning
		if (currentScript != null
			&& !SkriptConfig.disableVariableStartingWithExpressionWarnings.value()
			&& !currentScript.suppressesWarning(ScriptWarning.VARIABLE_STARTS_WITH_EXPRESSION)) {

			String strippedName = name;
			if (isLocal) {
				strippedName = strippedName.substring(LOCAL_VARIABLE_TOKEN.length());
			} else if (isEphemeral) {
				strippedName = strippedName.substring(EPHEMERAL_VARIABLE_TOKEN.length());
			}
			if (strippedName.startsWith("%")) {
				Skript.warning("Starting a variable's name with an expression is discouraged ({" + name + "}). " +
					"You could prefix it with the script's name: " +
					"{" + StringUtils.substring(currentScript.getConfig().getFileName(), 0, -3) + SEPARATOR + name + "}");
			}
		}

		// Check for local variable type hints
		if (isLocal && variableString.isSimple()) { // Only variable names we fully know already
			Set<Class<?>> hints = parser.getHintManager().get(variableString.toString(null));
			if (!hints.isEmpty()) { // Type hint(s) available
				if (types[0] == Object.class) { // Object is generic, so we initialize with the hints instead
					//noinspection unchecked
					return new Variable<>(variableString, hints.toArray(new Class[0]), true, isEphemeral, isPlural, null);
				}

				List<Class<? extends T>> potentialTypes = new ArrayList<>();

				// Determine what types are applicable based on our known hints
				for (Class<? extends T> type : types) {
					// Check whether we could resolve to 'type' at runtime
					if (hints.stream().anyMatch(hint -> type.isAssignableFrom(hint) || Converters.converterExists(hint, type))) {
						potentialTypes.add(type);
					}
				}
				if (!potentialTypes.isEmpty()) { // Hint matches, use variable with exactly correct type
					//noinspection unchecked
					return new Variable<>(variableString, potentialTypes.toArray(Class[]::new), true, isEphemeral, isPlural, null);
				}

				// Hint exists and does NOT match any types requested
				ClassInfo<?>[] infos = new ClassInfo[types.length];
				for (int i = 0; i < types.length; i++) {
					infos[i] = Classes.getSuperClassInfo(types[i]);
				}
				ClassInfo<?>[] hintInfos = hints.stream()
						.map(Classes::getSuperClassInfo)
						.toArray(ClassInfo[]::new);
				String isTypes = Utils.a(Classes.toString(hintInfos, false));
				String notTypes = Utils.a(Classes.toString(infos, false));
				Skript.error("Expected variable '{_" + variableString.toString(null) + "}' to be " + notTypes + ", but it is " + isTypes);
				return null;
			}
		}

		return new Variable<>(variableString, types, isLocal, isEphemeral, isPlural, null);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return Whether this variable is a local variable, i.e. starts with {@link #LOCAL_VARIABLE_TOKEN}.
	 */
	public boolean isLocal() {
		return local;
	}

	/**
	 * @return Whether this variable is an ephemeral variable, i.e. starts with {@link #EPHEMERAL_VARIABLE_TOKEN}.
	 */
	public boolean isEphemeral() {
		return ephemeral;
	}

	/**
	 * @return Whether this variable is a list variable, i.e. ends with {@link #SEPARATOR + "*"}.
	 */
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
	public Class<? extends T>[] possibleReturnTypes() {
		return Arrays.copyOf(types, types.length);
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
		boolean converterExists = superType == Object.class;
		if (!converterExists) {
			for (Class<?> type : types) {
				if (Converters.converterExists(type, to)) {
					converterExists = true;
					break;
				}
			}
		}
		if (!converterExists) {
			return null;
		}
		return new Variable<>(name, to, local, ephemeral, list, this);
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
		KeyedValue<?>[] values = listProvider.getValues(event);
		//noinspection unchecked,rawtypes
		return KeyedValue.unzip((KeyedValue[]) values).values().toArray();
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

	@Override
	public Iterator<KeyedValue<T>> keyedIterator(Event event) {
		if (!list)
			throw new SkriptAPIException("Invalid call to keyedIterator");
		Iterator<KeyedValue<?>> iterator = Iterators.forArray(listProvider.getValues(event));
		Iterator<KeyedValue<T>> transformed = Iterators.transform(iterator, value -> {
			assert value != null;
			T converted = Converters.convert(value.value(), types);
			if (converted == null)
				return null;
			return new KeyedValue<>(value.key(), converted);
		});
		return Iterators.filter(transformed, Objects::nonNull);
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
		//noinspection DataFlowIssue
		return Iterators.transform(keyedIterator(event), KeyedValue::value);
	}

	private @Nullable T getConverted(Event event) {
		assert !list;
		return Converters.convert(get(event), types);
	}

	private T[] getConvertedArray(Event event) {
		assert list;
		//noinspection unchecked
		KeyedValue<Object>[] values = (KeyedValue<Object>[]) listProvider.getValues(event);
		KeyedValue<T>[] mappedValues = KeyedValue.map(values, value -> Converters.convert(value, types));
		mappedValues = ArrayUtils.removeAllOccurrences(mappedValues, null);

		KeyedValue.UnzippedKeyValues<T> unzipped = KeyedValue.unzip(mappedValues);

		cache.put(event, unzipped.keys().toArray(new String[0]));
		//noinspection unchecked
		return unzipped.values().toArray((T[]) Array.newInstance(superType, 0));
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
	public void change(Event event, Object @NotNull [] delta, ChangeMode mode, @NotNull String @NotNull [] keys) {
		if (!list) {
			this.change(event, delta, mode);
			return;
		}
		if (mode == ChangeMode.SET) {
			assert delta.length == keys.length;
			this.set(event, null);
			int length = Math.min(delta.length, keys.length);
			for (int index = 0; index < length; index++) {
				Object value = delta[index];
				String key = keys[index];
				if (value instanceof Object[] array) {
					for (int j = 0; j < array.length; j++)
						this.setIndex(event, key + SEPARATOR + (j + 1), array[j]);
				} else {
					this.setIndex(event, key, value);
				}
			}
			return;
		}
		// no other modes are supported right now
		this.change(event, delta, mode);
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
						Set<String> toRemove = new HashSet<>(); // prevents CMEs
						for (Object value : delta) {
							for (Entry<String, Object> entry : map.entrySet()) {
								String key = entry.getKey();
								if (key == null)
									continue; // This is NOT a part of list variable
								if (toRemove.contains(key))
									continue; // Skip if we've already marked this key to be removed
								if (Relation.EQUAL.isImpliedBy(Comparators.compare(entry.getValue(), value))) {
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
						Set<String> toRemove = new HashSet<>(); // prevents CMEs
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

							Object value = originalValue == null ? Arithmetics.getDefaultValue(info.left()) : originalValue;
							if (value == null)
								continue;

							originalValue = info.operation().calculate(value, newValue);
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

	/**
	 * {@inheritDoc}
	 * @param getAll This has no effect for a Variable, as {@link #getArray(Event)} is the same as {@link #getAll(Event)}.
	 */
	@Override
	public <R> void changeInPlace(Event event, Function<T, R> changeFunction, boolean getAll) {
		changeInPlace(event, changeFunction);
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
		keyedIterator(event).forEachRemaining(keyedValue -> {
			String index = keyedValue.key();
			Object newValue = changeFunction.apply(keyedValue.value());
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
	public @NotNull String @NotNull [] getArrayKeys(Event event) throws SkriptAPIException {
		if (!list)
			throw new SkriptAPIException("Invalid call to getArrayKeys on non-list");
		if (!cache.containsKey(event))
			throw new IllegalStateException();
		return cache.remove(event);
	}

	@Override
	public @NotNull String @NotNull [] getAllKeys(Event event) {
		return this.getArrayKeys(event);
	}

	@Override
	public boolean canReturnKeys() {
		return list;
	}

	@Override
	public boolean areKeysRecommended() {
		return false; // We want `set {list::*} to {other::*}` reset numbering!
	}

	@Override
	public boolean returnNestedStructures(boolean nested) {
		if (!canReturnKeys())
			return false;
		listProvider = nested ? new RecursiveListProvider() : new ShallowListProvider();
		return true;
	}

	@Override
	public boolean returnsNestedStructures() {
		return listProvider.getClass() == RecursiveListProvider.class;
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
		return KeyProviderExpression.super.isLoopOf(input)
			|| input.equalsIgnoreCase("var")
			|| input.equalsIgnoreCase("variable")
			|| input.equalsIgnoreCase("value");
	}

	@Override
	public boolean check(Event event, Predicate<? super T> checker, boolean negated) {
		return SimpleExpression.check(getAll(event), checker, negated, getAnd());
	}

	@Override
	public boolean check(Event event, Predicate<? super T> checker) {
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

	private interface ListProvider {

		KeyedValue<?>[] getValues(Event event);

	}

	class ShallowListProvider implements ListProvider {

		@Override
		public KeyedValue<?>[] getValues(Event event) {
			if (!list)
				throw new SkriptAPIException("Invalid call to getValues on non-list variable");

			Object rawValue = getRaw(event);
			if (rawValue == null)
				return new KeyedValue[0];

			List<KeyedValue<?>> keyedValues = new ArrayList<>();
			String name = StringUtils.substring(Variable.this.name.toString(event), 0, -1);
			//noinspection unchecked
			for (Entry<String, ?> variable : ((Map<String, ?>) rawValue).entrySet()) {
				if (variable.getKey() == null || variable.getValue() == null)
					continue;

				Object value;
				if (variable.getValue() instanceof Map<?, ?> sublist) {
					value = sublist.get(null);
				} else {
					value = variable.getValue();
				}

				value = convertIfOldPlayer(name + variable.getKey(), local, event, value);
				if (value != null)
					keyedValues.add(new KeyedValue<>(variable.getKey(), value));
			}

			return keyedValues.toArray(new KeyedValue[0]);
		}

	}

	class RecursiveListProvider implements ListProvider {

		@Override
		public KeyedValue<?>[] getValues(Event event) {
			if (!list)
				throw new SkriptAPIException("Invalid call to getValues on non-list variable");

			Object rawValue = getRaw(event);
			if (rawValue == null)
				return new KeyedValue[0];

			List<KeyedValue<?>> keyedValues = new ArrayList<>();
			String name = StringUtils.substring(Variable.this.name.toString(event), 0, -1);
			getValuesRecursive(event, (Map<?, ?>) rawValue, name, "", keyedValues);

			return keyedValues.toArray(new KeyedValue[0]);
		}

		private void getValuesRecursive(Event event, Map<?, ?> variable, String root, String prefix, List<KeyedValue<?>> values) {
			//noinspection unchecked
			for (Entry<String, ?> entry : ((Map<String, ?>) variable).entrySet()) {
				if (entry.getKey() == null || entry.getValue() == null)
					continue;

				String relativeKey = prefix + entry.getKey();
				String absoluteKey = root + relativeKey;
				Object value;
				if (entry.getValue() instanceof Map<?, ?> sublist) {
					getValuesRecursive(event, (Map<?, ?>) entry.getValue(), root, relativeKey + SEPARATOR, values);
					value = sublist.get(null);
				} else {
					value = entry.getValue();
				}

				value = convertIfOldPlayer(absoluteKey, local, event, value);
				if (value != null)
					values.add(new KeyedValue<>(relativeKey, value));
			}
		}

	}

}
