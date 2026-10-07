package ch.njol.skript.lang.function;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.lang.util.common.AnyNamed;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Contract;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.Utils.PluralResult;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.function.FunctionBinder;
import org.skriptlang.skript.common.function.FunctionBinder.Mode;
import org.skriptlang.skript.common.function.FunctionReference;
import org.skriptlang.skript.common.function.FunctionReference.Argument;
import org.skriptlang.skript.common.function.FunctionReference.ArgumentType;
import org.skriptlang.skript.common.function.GenerationCache;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.util.Executable;
import org.skriptlang.skript.util.Result;
import org.skriptlang.skript.util.Validated;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A partial reference to a Skript function.
 * This reference knows some of its information in advance (such as the function's name)
 * but will not be resolved until it receives inputs for the first time.
 * <p>
 * Which overload of a function this refers to is therefore only decided once arguments are
 * supplied, unless the parameter types were given when the reference was obtained.
 * </p>
 *
 */
public class DynamicFunctionReference
	implements Contract, Executable<Event, Object[]>, Validated, AnyNamed {

	/**
	 * Splits a stringified function reference into its name and, if one was written, the list of
	 * parameter types selecting an overload, e.g. the 'integer, objects' of
	 * 'myFunction(integer, objects)'.
	 */
	private static final Pattern SIGNATURE_PATTERN = Pattern.compile("(?<name>[^(]+)\\((?<types>.*)\\).*");

	private final @NotNull String name;

	/**
	 * The namespace local functions are resolved in, or null to only resolve global functions.
	 */
	private final @Nullable String namespace;

	/**
	 * The declared parameter types of the only overload this may use, or null if whichever
	 * overload the supplied arguments select may be used.
	 */
	private final Class<?> @Nullable [] parameterTypes;

	/**
	 * Whether this resolved to a function local to {@link #namespace}, in which case it only ever
	 * refers to that script's own declaration. A reference which resolved to a global function is
	 * not tied to the script which happened to declare it: a global name identifies one function,
	 * whose overloads several scripts may declare between them.
	 */
	private final boolean local;

	/**
	 * The namespace of the signature this resolved to, which is where the function was declared
	 * rather than where it was looked up from. Only used to report where the function came from,
	 * through {@link #source()} and {@link #toString()}; which overloads this may use is decided
	 * by {@link #local} and {@link #accepts(Signature)}.
	 */
	private final @Nullable String declaredIn;

	private final Validated validator = Validated.validator();

	/**
	 * This function bound to each set of argument expressions it has been called with, so that
	 * each {@link CallSite} is reused rather than allocated per execution.
	 * <p>
	 * What a {@link CallSite} caches is which overload the expressions themselves select, which
	 * cannot change: an expression's return type and whether it is single are fixed when it is
	 * parsed. It deliberately does not cache an overload picked by spreading one argument's values
	 * across several parameters, since how many values there are decides which overload fits and
	 * that is only known per execution. See {@link #computeBinding(Input)}.
	 * </p>
	 * <p>
	 * Held in a {@link GenerationCache} so that reloading a script does not leave the call sites of
	 * the version before it behind. Each one holds that site's parsed expressions, and through them
	 * the script they were parsed from, so a reference kept in a global variable and called from a
	 * script reloaded repeatedly would otherwise accumulate one entry per reload forever.
	 * </p>
	 * <p>
	 * The cache only empties when a function is registered or removed, which a reload of a script
	 * declaring no functions does not do, so the capacity is what bounds that case rather than the
	 * generation. It is set well above the number of places one reference is realistically called
	 * from; a reference which somehow passes it keeps working, at the cost of building a call site
	 * per execution. Evicting the call sites of a script as it is unloaded would bound it properly,
	 * which needs a hook this does not have.
	 * </p>
	 */
	private final GenerationCache<Input, CallSite> callSites =
		GenerationCache.bounded(MAX_CALL_SITES);

	/**
	 * How many call sites one reference holds. See {@link #callSites}.
	 */
	private static final int MAX_CALL_SITES = 256;

	/**
	 * How many shapes of spread values one call site remembers the signature for.
	 */
	private static final int MAX_REMEMBERED_SPREADS = 16;

	/**
	 * How many resolved names are kept. Shared by every caller rather than held per expression, so
	 * this is more generous than a per-expression cache needed to be.
	 */
	private static final int MAX_CACHED_NAMES = 256;

	/**
	 * Names already resolved, so that resolving one again neither allocates a reference nor scans
	 * the registry. Shared by every caller, since what a name resolves to does not depend on who
	 * asked.
	 * <p>
	 * Keyed on the namespace rather than on the {@link Script} itself, which is all a reference
	 * keeps anyway. A reloaded script is a new {@code Script} object with the same file name, so
	 * keying on the object would both pin every unloaded version in memory and fill the cache with
	 * keys nothing will ask for again.
	 * </p>
	 * <p>
	 * The references handed out are shared, so {@link #invalidate()} on one disables it for every
	 * holder. Nothing in Skript calls it -- a reference is discarded by the generation moving
	 * instead -- but an addon which does would affect more than its own copy.
	 * </p>
	 */
	private static final GenerationCache<Key, DynamicFunctionReference> RESOLVED =
		GenerationCache.bounded(MAX_CACHED_NAMES);

	public DynamicFunctionReference(Function<?> function) {
		Signature<?> signature = function.getSignature();

		this.name = function.getName();
		this.namespace = signature.namespace();
		// a reference to a known function refers to exactly that overload
		this.parameterTypes = FunctionBinder.declaredTypes(signature);
		this.local = signature.isLocal();
		this.declaredIn = signature.namespace();
	}

	public DynamicFunctionReference(@NotNull String name) {
		this(name, null);
	}

	public DynamicFunctionReference(@NotNull String name, @Nullable Script source) {
		this(name, source, null);
	}

	/**
	 * @param name           The function name.
	 * @param source         The script the function is expected to be in, if one is known.
	 * @param parameterTypes The declared types of the parameters of the overload to use, or null
	 *                       to use whichever overload the supplied arguments select.
	 */
	public DynamicFunctionReference(@NotNull String name, @Nullable Script source, Class<?> @Nullable [] parameterTypes) {
		this(name, namespaceOf(source), parameterTypes);
	}

	/**
	 * @param name           The function name.
	 * @param namespace      The namespace to resolve a local function in, or null to resolve only
	 *                       global functions.
	 * @param parameterTypes The declared types of the parameters of the overload to use, or null
	 *                       to use whichever overload the supplied arguments select.
	 */
	private DynamicFunctionReference(@NotNull String name, @Nullable String namespace, Class<?> @Nullable [] parameterTypes) {
		this.name = name;
		this.namespace = namespace;
		this.parameterTypes = parameterTypes;

		Collection<Signature<?>> candidates = FunctionRegistry.getRegistry().getSignatures(namespace, name);
		this.local = resolvesLocally(candidates);
		this.declaredIn = local ? namespace : declaringNamespace(candidates);
	}

	/**
	 * Whether this reference may use {@code signature}.
	 * <p>
	 * This is the only rule for which overloads a reference may resolve to: the function must be
	 * the one it originally resolved to, since another script may later declare a global function
	 * of the same name, and it must be the pinned overload if one was given.
	 * </p>
	 *
	 * @param signature The signature.
	 * @return Whether this may resolve to {@code signature}.
	 */
	private boolean accepts(Signature<?> signature) {
		if (local) {
			// a local function is the one declared in the script this was looked up from, and
			// nothing else; a global function of the same name does not stand in for it
			if (!signature.isLocal() || !Objects.equals(signature.namespace(), namespace)) {
				return false;
			}
		} else if (signature.isLocal()) {
			// and conversely, a global reference never reaches into a script's local functions
			return false;
		}
		return parameterTypes == null
			|| Arrays.equals(FunctionBinder.declaredTypes(signature), parameterTypes);
	}

	/**
	 * Whether this resolved to a function local to the namespace it was looked up in. A local
	 * declaration shadows a global function of the same name, which is why this is asked before
	 * anything else.
	 *
	 * @param candidates The candidate signatures.
	 * @return Whether one of them is a local function of {@link #namespace}.
	 */
	private boolean resolvesLocally(Collection<Signature<?>> candidates) {
		if (namespace == null) { // only global functions were looked for
			return false;
		}
		for (Signature<?> candidate : candidates) {
			if (candidate.isLocal() && Objects.equals(candidate.namespace(), namespace)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Picks the namespace to report as the source of a global reference. Several scripts may
	 * declare overloads of one global name, so this prefers the one that was looked up and
	 * otherwise picks by namespace order rather than by iteration order, which
	 * {@link FunctionRegistry#getSignatures(String, String)} returns in a set whose order is
	 * randomised per JVM start.
	 *
	 * @param candidates The candidate signatures.
	 * @return The namespace to report, or null if that is not known.
	 */
	private @Nullable String declaringNamespace(Collection<Signature<?>> candidates) {
		String fallback = null;
		boolean first = true;
		for (Signature<?> candidate : candidates) {
			if (Objects.equals(candidate.namespace(), namespace)) {
				return namespace;
			}
			String candidateNamespace = candidate.namespace();
			if (first || compareNamespaces(candidateNamespace, fallback) < 0) {
				fallback = candidateNamespace;
				first = false;
			}
		}
		return fallback;
	}

	/**
	 * Orders two namespaces so that picking one of them is reproducible. A Java default function
	 * has no namespace at all, which sorts first.
	 *
	 * @param first  The first namespace, which may be null.
	 * @param second The second namespace, which may be null.
	 * @return A negative number if {@code first} sorts before {@code second}, zero if they are the
	 * 	same, a positive number otherwise.
	 */
	private static int compareNamespaces(@Nullable String first, @Nullable String second) {
		if (Objects.equals(first, second)) {
			return 0;
		}
		if (first == null) {
			return -1;
		}
		if (second == null) {
			return 1;
		}
		return first.compareTo(second);
	}

	/**
	 * Parses the parameter type list of a stringified function reference, e.g. the
	 * 'integer, objects' of 'myFunction(integer, objects)', into the declared parameter types it
	 * names. This is the inverse of how a signature is written out in an error message.
	 * <p>
	 * Deliberately silent. This runs for any unparsed literal shaped like {@code word(...)},
	 * because {@link Classes#parseSimple(String, Class, ParseContext)} offers every string to every
	 * {@link ClassInfo} parser in turn, so a line calling a function which does not exist reaches
	 * this. Reporting from here put "Cannot recognise the type 'x'" in the parse log, which
	 * replaced the real unknown-function error whenever the function {@code ClassInfo} happened to
	 * be the last one tried. Resolving a name which does not exist has always been silent.
	 * </p>
	 *
	 * @param types The type list. Must not be blank.
	 * @return The declared types, or null if any of them could not be recognised.
	 */
	private static Class<?> @Nullable [] parseParameterTypes(@NotNull String types) {
		// a type name contains no commas, and the function ClassInfo is only parsed in contexts
		// where SkriptParser#next offers no nesting protection, so a plain split is all we can do
		String[] split = types.split(",", -1);
		Class<?>[] parsed = new Class<?>[split.length];

		for (int i = 0; i < split.length; i++) {
			String type = split[i].trim();
			if (type.isEmpty()) {
				return null;
			}

			ClassInfo<?> classInfo = Classes.getClassInfoFromUserInput(type);
			PluralResult plural = Utils.isPlural(type);
			if (classInfo == null) {
				classInfo = Classes.getClassInfoFromUserInput(plural.updated());
			}

			if (classInfo == null) {
				return null;
			}

			parsed[i] = plural.plural() ? classInfo.getC().arrayType() : classInfo.getC();
		}

		return parsed;
	}

	public @Nullable Script source() {
		return ScriptLoader.getLoadedScriptFromName(declaredIn);
	}

	@Override
	public @NotNull String name() {
		return name;
	}

	@Override
	public boolean isSingle(Expression<?>... arguments) {
		FunctionReference<?> reference = binding(new Input(arguments)).reference();
		if (reference == null) {
			return true;
		}
		return reference.isSingle();
	}

	@Override
	public @Nullable Class<?> getReturnType(Expression<?>... arguments) {
		FunctionReference<?> reference = binding(new Input(arguments)).reference();
		if (reference == null) {
			return Object.class;
		}

		Contract contract = reference.signature().contract();
		if (contract != null) {
			return contract.getReturnType(arguments);
		}
		return reference.signature().returnType();
	}

	/**
	 * Executes this function with the given arguments.
	 *
	 * @param event The event to execute with.
	 * @param input The argument expressions, as collected when the caller was parsed.
	 * @return The returned values, or null if the arguments are not acceptable or execution failed.
	 */
	public Object @Nullable [] execute(Event event, Input input) {
		CallSite site = callSite(input);
		return call(event, site, site.resolution());
	}

	/**
	 * Executes this with the arguments of {@code site}.
	 * <p>
	 * Deliberately not called {@code execute}: {@link #execute(Event, Object...)} is varargs, so an
	 * overload of that name would silently take any call it did not match exactly.
	 * </p>
	 *
	 * @param event      The event to execute with.
	 * @param site       The call site whose arguments to use.
	 * @param resolution How those arguments bind to this function, together with the spread memo
	 *                   which belongs to that binding.
	 * @return The returned values, or null if the arguments are not acceptable or execution failed.
	 */
	private Object @Nullable [] call(Event event, CallSite site, Resolution resolution) {
		// deliberately not valid(): a binding is already discarded when the registered functions
		// change, so scanning the registry again here would only turn a function which no longer
		// exists into a silent failure, where binding it reports why
		if (!validator.valid()) {
			return null;
		}

		Binding binding = resolution.binding();
		FunctionReference<?> reference = binding.reference();

		if (reference == null) {
			if (binding.spread() < 0) { // the reason is available from BoundExecutable#rejection
				return null;
			}
			// the arguments only fit once the values of one of them are spread across several
			// parameters, which cannot be known before they have been evaluated
			reference = spread(event, site, resolution, binding.spread());
			if (reference == null) {
				return null;
			}
		}

		return run(event, reference);
	}

	/**
	 * Executes a bound reference.
	 *
	 * @param event     The event to execute with.
	 * @param reference The reference to execute.
	 * @return The returned values, or null if the function could not be obtained and so never ran.
	 * 	A function which ran and returned nothing gives an empty array.
	 */
	private Object @Nullable [] run(Event event, FunctionReference<?> reference) {
		// a reference whose function cannot be obtained did not run, and an empty array would make
		// that indistinguishable from a function which ran and returned nothing, so the caller
		// would report success for a call which never happened. Only the clearest case is caught
		// here: execute() also returns null when validation fails, which is not told apart
		if (reference.function() == null) {
			return null;
		}

		try {
			return normalise(reference, reference.execute(event));
		} finally {
			// FunctionReference#execute does not reset the return value itself, and a script
			// function asserts that its return value was reset before it is set again
			org.skriptlang.skript.common.function.Function<?> function = reference.function();
			if (function != null) {
				function.resetReturnValue();
			}
		}
	}

	/**
	 * @param reference The reference which was executed.
	 * @param result    The value the function returned.
	 * @return That value as an array, since a function may return either a single value or
	 * 	several, or an empty array if it returned nothing. Never null, so that a null result from
	 * 	{@link #execute(Event, Input)} only ever means the function did not run.
	 */
	private Object[] normalise(FunctionReference<?> reference, @Nullable Object result) {
		// a function returning several values already hands back an array of its own return type
		if (result instanceof Object[] array) {
			return array;
		}

		// the array is built with the function's return type as its component type, matching what
		// a function returning several values gives back, rather than always being an Object[]
		Class<?> returnType = reference.signature().returnType();
		Class<?> component = returnType != null ? Utils.getComponentType(returnType) : Object.class;

		Object[] array = (Object[]) Array.newInstance(component, result != null ? 1 : 0);
		if (result != null) {
			array[0] = result;
		}
		return array;
	}

	/**
	 * Binds the arguments of {@code input} by spreading the values of the argument at
	 * {@code index} across several parameters.
	 * <p>
	 * This exists so that passing a single list of values to a function of several parameters
	 * keeps working. How many parameters those values fill is only known once they have been
	 * evaluated, so unlike every other binding this one is worked out for each execution.
	 * </p>
	 *
	 * @param event      The event to evaluate with.
	 * @param site       The call site whose arguments to spread.
	 * @param resolution The resolution whose spread memo to use, which is the one belonging to the
	 *                   binding that asked for a spread.
	 * @param index      The index of the argument whose values are spread.
	 * @return The bound reference, or null if the values do not fit any overload either.
	 */
	private @Nullable FunctionReference<?> spread(
		Event event, CallSite site, Resolution resolution, int index
	) {
		Expression<?>[] expressions = site.input().expressions();

		// evaluated first so that both arrays below can be allocated at the size they need, since
		// this runs on every execution of the call site
		Object[] values = expressions[index].getArray(event);

		//noinspection unchecked
		Argument<Expression<?>>[] array =
			(Argument<Expression<?>>[]) new Argument[expressions.length - 1 + values.length];
		Class<?>[] types = new Class<?>[values.length];

		int next = 0;
		for (int i = 0; i < expressions.length; i++) {
			if (i != index) {
				array[next++] = argument(expressions[i]);
				continue;
			}

			for (int value = 0; value < values.length; value++) {
				// false: the second argument is isDefault, not the and/or of a list, and these
				// are values the caller passed rather than a parameter's default
				array[next++] = argument(new SimpleLiteral<>(values[value], false));
				types[value] = values[value].getClass();
			}
		}

		// a view rather than a copy: the key is only needed for the lookup, and a list compares
		// and hashes by its contents whatever backs it
		List<Class<?>> shape = Arrays.asList(types);

		// which overload a spread fits is decided by how many values there are and what types they
		// are, and by nothing else, so a call site reaching the same shape again binds straight to
		// the signature it bound to last time instead of searching the overloads once more
		Spread remembered = resolution.spread(shape);
		if (remembered != null) {
			FunctionBinder.Division division = FunctionBinder.forExpressions(Mode.GREEDY)
				.bindTo(namespace, name, remembered.signature(), remembered.allocation(), array);

			if (division != null) {
				FunctionReference<?> bound = division.reference();

				// binding to a remembered signature builds a new reference every execution, which
				// would look the function up in the registry again. Which function a signature
				// resolves to cannot change while the registry generation does not, and a change
				// discards this memo, so the call site hands the reference what it already knows
				if (remembered.function() != null) {
					bound.cacheFunction(remembered.function());
				}

				// which division of the arguments fits is only learnt by binding to the remembered
				// signature, so it is stored the first time that happens; from then on the call
				// site binds straight to it
				if (remembered.allocation() == null && division.allocation() != null) {
					resolution.rememberSpread(shape, remembered.signature(),
						division.allocation(), bound.function());
				}
				return bound;
			}
		}

		FunctionReference<?> reference = resolve(array).reference();
		// every signature the registry holds is a ch.njol Signature, which is the type the binder
		// takes; narrowing rather than casting leaves a shape unremembered instead of failing if
		// that ever stops holding
		if (reference != null && reference.signature() instanceof Signature<?> registered) {
			resolution.rememberSpread(shape, registered, null, reference.function());
		}
		return reference;
	}

	/**
	 * Resolves which overload {@code arguments} bind to.
	 *
	 * @param arguments The arguments to bind.
	 * @return The binding, which carries either the bound reference or why there is none.
	 */
	private Binding resolve(Argument<Expression<?>>[] arguments) {
		Result<? extends FunctionReference<?>> resolution =
			FunctionBinder.forExpressions(Mode.GREEDY)
				.resolve(namespace, name, arguments, this::accepts);

		return switch (resolution) {
			case Result.Success<? extends FunctionReference<?>>(var reference, var ignored) ->
				new Binding(reference, -1);
			case Result.Failure<? extends FunctionReference<?>>(var error) ->
				new Binding(null, -1, error);
		};
	}

	/**
	 * @param expressions The argument expressions.
	 * @return Those expressions as unnamed arguments, in the order they were given.
	 */
	private static Argument<Expression<?>>[] asArguments(Expression<?>... expressions) {
		//noinspection unchecked
		Argument<Expression<?>>[] arguments = (Argument<Expression<?>>[]) new Argument[expressions.length];
		for (int i = 0; i < expressions.length; i++) {
			arguments[i] = argument(expressions[i]);
		}
		return arguments;
	}

	/**
	 * @param input The argument expressions.
	 * @return How those arguments bind to this function, working it out if it is not yet known.
	 */
	private Binding binding(Input input) {
		return callSite(input).binding();
	}

	/**
	 * @param input The argument expressions.
	 * @return This function bound to those arguments, which is the same object every time so that
	 * the binding it works out is shared by everything asking about the same arguments.
	 */
	private CallSite callSite(Input input) {
		// the cache only hands back null when the value could not be worked out, and a call site
		// always can
		return Objects.requireNonNull(callSites.get(input, CallSite::new));
	}

	/**
	 * Works out how the arguments of {@code input} bind to this function.
	 *
	 * @param input The argument expressions.
	 * @return The binding, which may be one that does not fit.
	 */
	private Binding computeBinding(Input input) {
		Expression<?>[] expressions = input.expressions();

		Binding binding = resolve(asArguments(expressions));
		if (binding.reference() != null) {
			return binding;
		}

		// an argument which is not single may have to be spread across several parameters, which
		// can only be decided once its values are known, so this failure is not yet final. The
		// reason is kept even so: if the spread does not fit either, it is the best explanation
		// available, since working out why the values did not fit would mean evaluating them again
		int spread = spreadable(expressions);
		return spread >= 0 ? new Binding(null, spread, binding.error()) : binding;
	}


	/**
	 * @param expressions The argument expressions.
	 * @return The index of the only argument whose values could be spread across several
	 * 	parameters, or -1 if there is not exactly one such argument.
	 */
	private static int spreadable(Expression<?>[] expressions) {
		int spread = -1;
		for (int i = 0; i < expressions.length; i++) {
			if (expressions[i].isSingle()) {
				continue;
			}
			if (spread >= 0) { // with more than one, which to spread would be arbitrary
				return -1;
			}
			spread = i;
		}
		return spread;
	}

	private static Argument<Expression<?>> argument(Expression<?> expression) {
		return new Argument<>(ArgumentType.UNNAMED, null, expression);
	}

	@Override
	public @Nullable Executable.BoundExecutable<Event, Object[]> bind(Expression<?>... arguments) {
		// a function always decides for itself which parameter each argument belongs to, and
		// whether they are acceptable may depend on their values, so that is left to execution
		return callSite(new Input(arguments));
	}

	@Override
	public Object @Nullable [] execute(Event event, Object... arguments) {
		Expression<?>[] expressions = new Expression[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			expressions[i] = literal(arguments[i]);
		}

		// the values are new on every call, so there is nothing worth caching here
		FunctionReference<?> reference = resolve(asArguments(expressions)).reference();
		return reference != null ? run(event, reference) : null;
	}

	/**
	 * Represents an argument value passed to the positional form of
	 * {@link #execute(Event, Object...)} as an expression.
	 *
	 * @param value The value, which may be an array of values for a list parameter, or null to
	 *              leave the parameter to its default.
	 * @return The expression, or null if the parameter should use its default.
	 */
	private static @Nullable Expression<?> literal(@Nullable Object value) {
		if (value == null) { // the parameter was omitted
			return null;
		}

		if (value instanceof Object[] values) { // several values, for a list parameter
			return new SimpleLiteral<>(values, Object.class, true);
		}

		return new SimpleLiteral<>(value, false); // isDefault, not and/or; see spread()
	}

	@Override
	public void invalidate() {
		this.validator.invalidate();
	}

	@Override
	public boolean valid() {
		if (!validator.valid()) {
			return false;
		}

		for (Signature<?> signature : FunctionRegistry.getRegistry().getSignatures(namespace, name)) {
			if (accepts(signature)) {
				return true;
			}
		}
		return false;
	}


	@Override
	public String toString() {
		String signature = name + "(" + pinnedTypes() + ")";

		Script source = source();
		if (source != null)
			return signature + " from " + Classes.toString(source);
		return signature;
	}

	/**
	 * The parameter types this is pinned to, written the way they would be read back, so that
	 * stringifying a reference and parsing it again keeps the overload it was pinned to.
	 * <p>
	 * Empty when no overload is pinned, since {@code myFunction()} has always meant the function of
	 * that name whatever its parameters. This is the inverse of
	 * {@link #parseParameterTypes(String)}.
	 * </p>
	 *
	 * @return The type list, or an empty string if whichever overload the arguments select may be
	 * 	used.
	 */
	private String pinnedTypes() {
		if (parameterTypes == null) {
			return "";
		}

		StringJoiner joiner = new StringJoiner(", ");
		for (Class<?> type : parameterTypes) {
			joiner.add(typeName(type));
		}
		return joiner.toString();
	}

	/**
	 * @param type A declared parameter type.
	 * @return The name that type is written with, plural for an array, which is the form
	 * 	{@link #parseParameterTypes(String)} reads back into it.
	 */
	private static String typeName(Class<?> type) {
		Class<?> component = Utils.getComponentType(type);

		// the exact type where one is registered, so that the name read back names the same class;
		// a parameter of an unregistered type still has to be written as something
		ClassInfo<?> classInfo = Classes.getExactClassInfo(component);
		if (classInfo == null) {
			classInfo = Classes.getSuperClassInfo(component);
		}

		return type.isArray() ? classInfo.getName().getPlural() : classInfo.getName().getSingular();
	}

	/**
	 * Validates whether dynamic inputs are appropriate for the resolved function.
	 *
	 * @param parameters The input types to check
	 * @return A combined expression list, if these inputs are appropriate for the function
	 * @deprecated Use {@link #execute(Event, Input)}, which binds each argument to the parameter
	 * 	it belongs to instead of collapsing them all into one expression.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public @Nullable Expression<?> validate(Expression<?>[] parameters) {
		return this.validate(new Input(parameters));
	}

	/**
	 * @deprecated Use {@link #execute(Event, Input)}, which binds each argument to the parameter
	 * 	it belongs to instead of collapsing them all into one expression.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public @Nullable Expression<?> validate(Input input) {
		Binding binding = binding(input);
		if (!binding.bindable()) {
			return null;
		}

		FunctionReference<?> reference = binding.reference();
		if (reference == null) {
			// the arguments only fit once the values of one of them are spread across several
			// parameters, which is not known until they have been evaluated and there is no event
			// here to evaluate them with. The expressions as they were passed are all there is
			return new ExpressionList<>(input.expressions(), Object.class, true);
		}

		// each argument converted to the type of the parameter it bound to, which is what this
		// returned before the rewrite. Handing back the expressions as they were passed would give
		// an addon still calling this a value of the wrong type, with no compile error to warn it.
		// Two differences from the original remain: these are in parameter order rather than the
		// order they were passed, and a parameter left to its default is not among them
		Expression<?>[] converted = reference.convertedArguments();
		if (converted == null) {
			return null;
		}

		// the arguments are acceptable, but which parameter each of them belongs to is only known
		// to the binding, so handing them back as one expression loses that
		return new ExpressionList<>(converted, Object.class, true);
	}

	/**
	 * Attempts to parse a function reference from the format it would be stringified in.
	 * The name can include the source script name for the case of parsing local functions.
	 * @param name The function name, possibly including its script name
	 * @return A reference, if one is available
	 */
	public static @Nullable DynamicFunctionReference parseFunction(String name) {
		// Function reference string-ifying appends a () and potentially its source,
		// e.g. `myFunction() from MyScript.sk` and we should turn that into a valid function.
		if (name.contains(") from ")) {
			// The user might be trying to resolve a local function by name only
			String source = name.substring(name.lastIndexOf(" from ") + 6).trim();
			Script script = ScriptLoader.getLoadedScriptFromName(source);
			return resolveFunction(name.substring(0, name.lastIndexOf(" from ")).trim(), script);
		}
		return resolveFunction(name, null);
	}

	/**
	 * Used to resolve a function from its name.
	 * <p>
	 * A name resolved before is handed back the same reference rather than resolved again, since
	 * this is evaluated once per execution wherever a function is looked up by name at runtime.
	 * </p>
	 *
	 * @param name The function name
	 * @param script Potentially, the script it is from, if one is known
	 * @return A function reference, if one is available.
	 */
	public static @Nullable DynamicFunctionReference resolveFunction(String name, @Nullable Script script) {
		// a name which resolves to nothing is not kept: calling a function which does not exist is
		// not a case worth making fast, and the cache has nowhere to put an absence
		return RESOLVED.get(new Key(name, namespaceOf(script)),
			key -> resolveUncached(key.name(), key.namespace()));
	}

	/**
	 * @param script The script, or null.
	 * @return The namespace local functions of {@code script} are registered in, or null to resolve
	 * 	only global functions.
	 */
	private static @Nullable String namespaceOf(@Nullable Script script) {
		return script != null ? script.getConfig().getFileName() : null;
	}

	/**
	 * A function name resolved against a particular namespace.
	 *
	 * @param name      The function name as it was written, which may name a particular overload.
	 * @param namespace The namespace a local function was looked for in, or null for a global one.
	 */
	private record Key(String name, @Nullable String namespace) {

	}

	/**
	 * Resolves a function from its name, without consulting {@link #RESOLVED}.
	 *
	 * @param name The function name.
	 * @param namespace The namespace to resolve a local function in, or null for a global one.
	 * @return A function reference, if one is available.
	 */
	private static @Nullable DynamicFunctionReference resolveUncached(String name, @Nullable String namespace) {
		Class<?>[] parameterTypes = null;

		Matcher matcher = SIGNATURE_PATTERN.matcher(name);
		if (matcher.matches()) {
			String types = matcher.group("types");
			name = matcher.group("name").trim();

			// 'myFunction()' has always meant the function of that name, whatever its parameters,
			// so only a non-empty list selects a particular overload
			if (!types.isBlank()) {
				parameterTypes = parseParameterTypes(types);
				if (parameterTypes == null) {
					return null;
				}
			}
		}

		DynamicFunctionReference reference = new DynamicFunctionReference(name, namespace, parameterTypes);
		if (!reference.valid())
			return null;
		return reference;
	}

	/**
	 * This function together with one particular set of argument expressions, holding which
	 * overload they resolved to and resolving them again when the registered functions change.
	 */
	private final class CallSite implements BoundExecutable<Event, Object[]> {

		private final Input input;

		/**
		 * The binding, the spread memo belonging to it and the registry generation both were
		 * resolved against, replaced as one object so that a reader never sees any of the three
		 * beside the wrong generation.
		 */
		private volatile @Nullable Resolution resolution;

		private CallSite(Input input) {
			this.input = input;
		}

		private Input input() {
			return input;
		}

		/**
		 * @return How the arguments bind to this function, working it out again if the registered
		 * functions have changed since it was last worked out. The spread memo comes with it, so a
		 * caller cannot read one beside a binding worked out against different overloads.
		 */
		private Resolution resolution() {
			// a reload replaces the functions this may have bound to, and may equally have added
			// the overload a previously failed binding was looking for, so both outcomes are
			// discarded
			long current = FunctionRegistry.getRegistry().generation();

			Resolution resolution = this.resolution;
			if (resolution != null && resolution.generation() == current) {
				return resolution;
			}

			// a new resolution brings an empty spread memo with it, so whatever the shapes bound
			// to under the previous overloads goes with it rather than having to be cleared
			resolution = new Resolution(computeBinding(input), current);
			this.resolution = resolution;
			return resolution;
		}

		@Override
		public Object @Nullable [] execute(Event event) {
			return DynamicFunctionReference.this.call(event, this, resolution());
		}

		/**
		 * @return How the arguments bind to this function, for a caller which does not need the
		 * 	spread memo that goes with it.
		 */
		private Binding binding() {
			return resolution().binding();
		}

		@Override
		public @NotNull String rejection() {
			if (!validator.valid()) {
				return "The function " + DynamicFunctionReference.this + " is no longer available.";
			}

			// for a binding which needed a spread this is why the arguments did not fit one per
			// parameter, which is what made the spread necessary in the first place
			String error = binding().error();
			if (error != null) {
				return error;
			}

			return "Cannot run " + DynamicFunctionReference.this + " with the given arguments.";
		}

	}

	/**
	 * A binding and the state of the function registry it was resolved against.
	 *
	 * @param binding    How the arguments bound to this function.
	 * @param generation The {@link FunctionRegistry#generation()} the binding was resolved against.
	 * @param spreads    What a spread of values of particular types bound to, so that executing the
	 *                   call site again with the same shape of values searches neither the overloads
	 *                   nor the divisions. Part of the resolution rather than kept beside it: a
	 *                   remembered shape is only meaningful for the overloads the binding was
	 *                   worked out against, so the two are replaced together.
	 */
	private record Resolution(
		Binding binding, long generation, Map<List<Class<?>>, Spread> spreads
	) {

		private Resolution(Binding binding, long generation) {
			this(binding, generation, new ConcurrentHashMap<>());
		}

		/**
		 * @param shape The types of the values being spread, in order.
		 * @return What that shape bound to before, or null if it has not been seen.
		 */
		private @Nullable Spread spread(List<Class<?>> shape) {
			return spreads.get(shape);
		}

		/**
		 * @param shape      The types of the values which were spread, in order.
		 * @param signature  The signature they bound to.
		 * @param allocation How many of them each parameter took, or null if that is not known yet.
		 * @param function   The function {@code signature} resolves to, or null if it could not be
		 *                   resolved.
		 */
		private void rememberSpread(
			List<Class<?>> shape, Signature<?> signature, int @Nullable [] allocation,
			@Nullable org.skriptlang.skript.common.function.Function<?> function
		) {
			// a call site which reaches more shapes than this keeps the ones it already holds,
			// which are the ones it has seen most, rather than discarding all of them: a memo
			// cleared whenever it fills up would stop paying off for a site that overfills it once.
			// An update to a shape already remembered is always let through
			if (spreads.size() >= MAX_REMEMBERED_SPREADS && !spreads.containsKey(shape)) {
				return;
			}
			spreads.put(List.copyOf(shape), new Spread(signature, allocation, function));
		}

	}

	/**
	 * What one shape of spread values bound to.
	 *
	 * @param signature  The signature the values bound to.
	 * @param allocation How many of the values each parameter took, or null until binding to
	 *                   {@code signature} has worked that out once.
	 * @param function   The function {@code signature} resolves to, kept so that re-binding to it
	 *                   does not look it up in the registry again. Null if it could not be
	 *                   resolved. Only valid while the registry generation which bound it holds,
	 *                   which is what {@link CallSite#resolution()} replacing the whole resolution
	 *                   guarantees.
	 */
	private record Spread(
		Signature<?> signature, int @Nullable [] allocation,
		@Nullable org.skriptlang.skript.common.function.Function<?> function
	) {

	}

	/**
	 * How a set of argument expressions binds to a function.
	 *
	 * @param reference The reference the arguments were bound to, or null if they could not be
	 *                  bound without knowing their values.
	 * @param spread    The index of the argument whose values must be spread across several
	 *                  parameters for the arguments to fit, or -1 if there is none.
	 * @param error     Why the arguments could not be bound, if they could not be.
	 */
	private record Binding(@Nullable FunctionReference<?> reference, int spread, @Nullable String error) {

		private Binding(@Nullable FunctionReference<?> reference, int spread) {
			this(reference, spread, null);
		}

		/**
		 * @return Whether the arguments fit, possibly only once their values are known.
		 */
		private boolean bindable() {
			return reference != null || spread >= 0;
		}

	}

	/**
	 * A key for a particular set of argument expressions, so that they are only bound once rather
	 * than on every execution.
	 * <p>
	 * Two inputs are the same when they hold the same expression objects. No
	 * {@link Expression} implementation overrides {@link Object#equals(Object)}, so this compares
	 * them by identity, which is what makes an input stand for the place the call was written.
	 * The types the expressions return are deliberately not part of the key: they are derived from
	 * those same objects and are fixed once parsed, so they could only ever agree.
	 * </p>
	 */
	public static class Input {

		private final Expression<?>[] expressions;

		public Input(Expression<?>... expressions) {
			this.expressions = expressions;
		}

		private Expression<?>[] expressions() {
			return expressions;
		}

		@Override
		public boolean equals(Object object) {
			if (this == object)
				return true;
			if (!(object instanceof Input input))
				return false;
			return Arrays.equals(expressions, input.expressions);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(expressions);
		}

	}

}
