package ch.njol.skript.classes;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Debuggable;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Noun;
import ch.njol.util.coll.iterator.ArrayIterator;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.Operator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Peter Güttinger
 * @param <T> The class this info is for
 */
@SuppressFBWarnings("DM_STRING_VOID_CTOR")
public class ClassInfo<T> implements Debuggable {

	private final Class<T> c;
	private final String codeName;
	private final Noun name;

	@Nullable
	private DefaultExpression<T> defaultExpression = null;

	@Nullable
	private Parser<? extends T> parser = null;

	@Nullable
	private Cloner<T> cloner = null;

	@Nullable Pattern[] userInputPatterns = null;

	@Nullable
	private Changer<? super T> changer = null;

	@Nullable
	private Supplier<Iterator<T>> supplier = null;

	@Nullable
	private Serializer<? super T> serializer = null;
	@Nullable
	private Class<?> serializeAs = null;

	@Nullable
	private Arithmetic<? super T, ?> math = null;
	@Nullable
	private Class<?> mathRelativeType = null;

	@Nullable
	private String docName = null;
	@Nullable
	private String[] description = null;
	@Nullable
	private String[] usage = null;
	@Nullable
	private String[] examples = null;
	@Nullable
	private String since = null;
	@Nullable
	private String[] requiredPlugins = null;

	/**
	 * Overrides documentation id assigned from class name.
	 */
	@Nullable
	private String documentationId = null;

	/**
	 * @param c The class
	 * @param codeName The name used in patterns
	 */
	public ClassInfo(final Class<T> c, final String codeName) {
		this.c = c;
		if (!isValidCodeName(codeName))
			throw new IllegalArgumentException("Code names for classes must be lowercase and only consist of latin letters and arabic numbers");
		this.codeName = codeName;
		name = new Noun("types." + codeName);
	}

	/**
	 * Incorrect spelling in method name. This will be removed in the future.
	 */
	@Deprecated
	public static boolean isVaildCodeName(final String name) {
		return isValidCodeName(name);
	}

	public static boolean isValidCodeName(final String name) {
		return name.matches("(?:any-)?[a-z0-9]+");
	}

	// === FACTORY METHODS ===

	/**
	 * @param parser A parser to parse values of this class or null if not applicable
	 */
	public ClassInfo<T> parser(final Parser<? extends T> parser) {
		assert this.parser == null;
		this.parser = parser;
		return this;
	}

	/**
	 * @param cloner A {@link Cloner} to clone values when setting variables
	 *                  or passing function arguments.
	 */
	public ClassInfo<T> cloner(Cloner<T> cloner) {
		assert this.cloner == null;
		this.cloner = cloner;
		return this;
	}

	/**
	 * @param userInputPatterns <u>Regex</u> patterns to match this class, e.g. in the expressions loop-[type], random [type] out of ..., or as command arguments. These patterns
	 *            must be english and match singular and plural.
	 * @throws PatternSyntaxException If any of the patterns' syntaxes is invalid
	 */
	public ClassInfo<T> user(final String... userInputPatterns) throws PatternSyntaxException {
		assert this.userInputPatterns == null;
		this.userInputPatterns = new Pattern[userInputPatterns.length];
		for (int i = 0; i < userInputPatterns.length; i++) {
			assert this.userInputPatterns != null;
			this.userInputPatterns[i] = Pattern.compile(userInputPatterns[i]);
		}
		return this;
	}

	/**
	 * @param defaultExpression The default (event) value of this class or null if not applicable
	 * @see EventValueExpression
	 * @see SimpleLiteral
	 */
	public ClassInfo<T> defaultExpression(final DefaultExpression<T> defaultExpression) {
		assert this.defaultExpression == null;
		if (!defaultExpression.isDefault())
			throw new IllegalArgumentException("defaultExpression.isDefault() must return true for the default expression of a class");
		this.defaultExpression = defaultExpression;
		return this;
	}


	/**
	 * Used for dynamically getting all the possible values of a class
	 *
	 * @param supplier The supplier of the values
	 * @return This ClassInfo object
	 * @see ClassInfo#supplier(Object[])
	 */
	public ClassInfo<T> supplier(Supplier<Iterator<T>> supplier) {
		if (this.supplier != null)
			throw new SkriptAPIException("supplier of this class is already set");
		this.supplier = supplier;
		return this;
	}

	/**
	 * Used for getting all the possible constants of a class
	 *
	 * @param values The array of the values
	 * @return This ClassInfo object
	 * @see ClassInfo#supplier(Supplier)
	 */
	public ClassInfo<T> supplier(T[] values) {
		return supplier(() -> new ArrayIterator<>(values));
	}

	public ClassInfo<T> serializer(final Serializer<? super T> serializer) {
		assert this.serializer == null;
		if (serializeAs != null)
			throw new IllegalStateException("Can't set a serializer if this class is set to be serialized as another one");
		this.serializer = serializer;
		serializer.register(this);
		return this;
	}

	public ClassInfo<T> serializeAs(final Class<?> serializeAs) {
		assert this.serializeAs == null;
		if (serializer != null)
			throw new IllegalStateException("Can't set this class to be serialized as another one if a serializer is already set");
		this.serializeAs = serializeAs;
		return this;
	}

	@Deprecated
	public ClassInfo<T> changer(final SerializableChanger<? super T> changer) {
		return changer((Changer<? super T>) changer);
	}

	public ClassInfo<T> changer(final Changer<? super T> changer) {
		assert this.changer == null;
		this.changer = changer;
		return this;
	}

	@Deprecated
	@SuppressWarnings("unchecked")
	public <R> ClassInfo<T> math(final Class<R> relativeType, final Arithmetic<? super T, R> math) {
		assert this.math == null;
		this.math = math;
		mathRelativeType = relativeType;
		Arithmetics.registerOperation(Operator.ADDITION, c, relativeType, (left, right) -> (T) math.add(left, right));
		Arithmetics.registerOperation(Operator.SUBTRACTION, c, relativeType, (left, right) -> (T) math.subtract(left, right));
		Arithmetics.registerOperation(Operator.MULTIPLICATION, c, relativeType, (left, right) -> (T) math.multiply(left, right));
		Arithmetics.registerOperation(Operator.DIVISION, c, relativeType, (left, right) -> (T) math.divide(left, right));
		Arithmetics.registerOperation(Operator.EXPONENTIATION, c, relativeType, (left, right) -> (T) math.power(left, right));
		Arithmetics.registerDifference(c, relativeType, math::difference);
		return this;
	}

	/**
	 * Use this as {@link #name(String)} to suppress warnings about missing documentation.
	 */
	public final static String NO_DOC = new String();

	/**
	 * Only used for Skript's documentation.
	 *
	 * @param name
	 * @return This ClassInfo object
	 */
	public ClassInfo<T> name(final String name) {
		assert this.docName == null;
		this.docName = name;
		return this;
	}

	/**
	 * Only used for Skript's documentation.
	 *
	 * @param description
	 * @return This ClassInfo object
	 */
	public ClassInfo<T> description(final String... description) {
		assert this.description == null;
		this.description = description;
		return this;
	}

	/**
	 * Only used for Skript's documentation.
	 *
	 * @param usage
	 * @return This ClassInfo object
	 */
	public ClassInfo<T> usage(final String... usage) {
		assert this.usage == null;
		this.usage = usage;
		return this;
	}

	/**
	 * Only used for Skript's documentation.
	 *
	 * @param examples
	 * @return This ClassInfo object
	 */
	public ClassInfo<T> examples(final String... examples) {
		assert this.examples == null;
		this.examples = examples;
		return this;
	}

	/**
	 * Only used for Skript's documentation.
	 *
	 * @param since
	 * @return This ClassInfo object
	 */
	public ClassInfo<T> since(final String since) {
		assert this.since == null;
		this.since = since;
		return this;
	}

	/**
	 * Other plugin dependencies for this ClassInfo.
	 *
	 * Only used for Skript's documentation.
	 *
	 * @param pluginNames
	 * @return This ClassInfo object
	 */
	public ClassInfo<T> requiredPlugins(final String... pluginNames) {
		assert this.requiredPlugins == null;
		this.requiredPlugins = pluginNames;
		return this;
	}

	/**
	 * Overrides default documentation id, which is assigned from class name.
	 * This is especially useful for inner classes whose names are useless without
	 * parent class name as a context.
	 * @param id Documentation id override.
	 * @return This ClassInfo object.
	 */
	public ClassInfo<T> documentationId(String id) {
		assert this.documentationId == null;
		this.documentationId = id;
		return this;
	}

	// === GETTERS ===

	public Class<T> getC() {
		return c;
	}

	public Noun getName() {
		return name;
	}

	public String getCodeName() {
		return codeName;
	}

	@Nullable
	public DefaultExpression<T> getDefaultExpression() {
		return defaultExpression;
	}

	@Nullable
	public Parser<? extends T> getParser() {
		return parser;
	}

	@Nullable
	public Cloner<? extends T> getCloner() {
		return cloner;
	}

	/**
	 * Clones the given object using {@link ClassInfo#cloner},
	 * returning the given object if no {@link Cloner} is registered.
	 */
	public T clone(T t) {
		return cloner == null ? t : cloner.clone(t);
	}

	@Nullable
	public Pattern[] getUserInputPatterns() {
		return userInputPatterns;
	}

	@Nullable
	public Changer<? super T> getChanger() {
		return changer;
	}

	@Nullable
	public Supplier<Iterator<T>> getSupplier() {
		if (supplier == null && c.isEnum())
			supplier = () -> new ArrayIterator<>(c.getEnumConstants());
		return supplier;
	}

	@Nullable
	public Serializer<? super T> getSerializer() {
		return serializer;
	}

	@Nullable
	public Class<?> getSerializeAs() {
		return serializeAs;
	}

	@Nullable
	@Deprecated
	public Arithmetic<? super T, ?> getMath() {
		return math;
	}

	@Nullable
	@Deprecated
	public <R> Arithmetic<T, R> getRelativeMath() {
		return (Arithmetic<T, R>) math;
	}

	@Nullable
	@Deprecated
	public Class<?> getMathRelativeType() {
		return mathRelativeType;
	}

	@Nullable
	public String[] getDescription() {
		return description;
	}

	@Nullable
	public String[] getUsage() {
		return usage;
	}

	@Nullable
	public String[] getExamples() {
		return examples;
	}

	@Nullable
	public String getSince() {
		return since;
	}

	@Nullable
	public String getDocName() {
		return docName;
	}

	@Nullable
	public String[] getRequiredPlugins() {
		return requiredPlugins;
	}

	/**
	 * Gets overridden documentation id of this this type. If no override has
	 * been set, null is returned and the caller may try to derive this from
	 * name of {@code #getC()}.
	 * @return Documentation id override, or null.
	 */
	@Nullable
	public String getDocumentationID() {
		return documentationId;
	}

	public boolean hasDocs() {
		return getDocName() != null && !ClassInfo.NO_DOC.equals(getDocName());
	}

	// === ORDERING ===

	@Nullable
	private Set<String> before;
	private final Set<String> after = new HashSet<>();

	/**
	 * Sets one or more classes that this class should occur before in the class info list. This only affects the order in which classes are parsed if it's unknown of which type
	 * the parsed string is.
	 * <p>
	 * Please note that subclasses will always be registered before superclasses, no matter what is defined here or in {@link #after(String...)}.
	 * <p>
	 * This list can safely contain classes that may not exist.
	 *
	 * @param before
	 * @return this ClassInfo
	 */
	public ClassInfo<T> before(final String... before) {
		assert this.before == null;
		this.before = new HashSet<>(Arrays.asList(before));
		return this;
	}

	/**
	 * Sets one or more classes that this class should occur after in the class info list. This only affects the order in which classes are parsed if it's unknown of which type
	 * the parsed string is.
	 * <p>
	 * Please note that subclasses will always be registered before superclasses, no matter what is defined here or in {@link #before(String...)}.
	 * <p>
	 * This list can safely contain classes that may not exist.
	 *
	 * @param after
	 * @return this ClassInfo
	 */
	public ClassInfo<T> after(final String... after) {
		this.after.addAll(Arrays.asList(after));
		return this;
	}

	/**
	 * @return Set of classes that should be after this one. May return null.
	 */
	@Nullable
	public Set<String> before() {
		return before;
	}

	/**
	 * @return Set of classes that should be before this one. Never returns null.
	 */
	public Set<String> after() {
		return after;
	}

	// === GENERAL ===

	@Override
	@NotNull
	public String toString() {
		return getName().getSingular();
	}

	public String toString(final int flags) {
		return getName().toString(flags);
	}

	@Override
	@NotNull
	public String toString(final @Nullable Event event, final boolean debug) {
		if (debug)
			return codeName + " (" + c.getCanonicalName() + ")";
		return getName().getSingular();
	}

}
