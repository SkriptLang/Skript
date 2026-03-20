package ch.njol.skript.registrations;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.PatternedParser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.command.Commands;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.localization.Language;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.JdbcStorage;
import ch.njol.skript.variables.SerializedVariable;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.yggdrasil.Tag;
import ch.njol.yggdrasil.Yggdrasil;
import ch.njol.yggdrasil.YggdrasilOutputStream;
import com.google.common.base.Preconditions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.*;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.properties.Property;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @author Peter Güttinger
 */
public abstract class Classes {

	private Classes() {}

	@Nullable
	private static ClassInfo<?>[] classInfos = null;
	private final static List<ClassInfo<?>> tempClassInfos = new ArrayList<>();
	private final static HashMap<Class<?>, ClassInfo<?>> exactClassInfos = new HashMap<>();
	private final static HashMap<Class<?>, ClassInfo<?>> superClassInfos = new HashMap<>();
	private final static HashMap<String, ClassInfo<?>> classInfosByCodeName = new HashMap<>();
	private final static Map<String, List<ClassInfo<?>>> registeredLiteralPatterns = new HashMap<>();

	/**
	 * @param info info about the class to register
	 */
	public static <T> void registerClass(final ClassInfo<T> info) {
		try {
			Skript.checkAcceptRegistrations();
			if (classInfosByCodeName.containsKey(info.getCodeName()))
				throw new IllegalArgumentException("Can't register " + info.getC().getName() + " with the code name " + info.getCodeName() + " because that name is already used by " + classInfosByCodeName.get(info.getCodeName()));
			if (exactClassInfos.containsKey(info.getC()))
				throw new IllegalArgumentException("Can't register the class info " + info.getCodeName() + " because the class " + info.getC().getName() + " is already registered");
			if (info.getCodeName().length() > JdbcStorage.MAX_CLASS_CODENAME_LENGTH)
				throw new IllegalArgumentException("The codename '" + info.getCodeName() + "' is too long to be saved in a database, the maximum length allowed is " + JdbcStorage.MAX_CLASS_CODENAME_LENGTH);
			exactClassInfos.put(info.getC(), info);
			classInfosByCodeName.put(info.getCodeName(), info);
			tempClassInfos.add(info);
			if (info.getParser() instanceof PatternedParser<?> patternedParser) {
				String[] patterns = patternedParser.getPatterns();
				for (String pattern : patterns) {
					registeredLiteralPatterns.computeIfAbsent(pattern, list -> new ArrayList<>()).add(info);
				}
			}
		} catch (RuntimeException e) {
			if (SkriptConfig.apiSoftExceptions.value())
				Skript.warning("Ignored an exception due to user configuration: " + e.getMessage());
			else
				throw e;
		}
	}

	public static void onRegistrationsStop() {

		sortClassInfos();

		// validate serializeAs
		for (final ClassInfo<?> ci : getClassInfos()) {
			if (ci.getSerializeAs() != null) {
				final ClassInfo<?> sa = getExactClassInfo(ci.getSerializeAs());
				if (sa == null) {
					Skript.error(ci.getCodeName() + "'s 'serializeAs' class is not registered");
				} else if (sa.getSerializer() == null) {
					Skript.error(ci.getCodeName() + "'s 'serializeAs' class is not serializable");
				}
			}
		}

		// register to Yggdrasil
		for (final ClassInfo<?> ci : getClassInfos()) {
			final Serializer<?> s = ci.getSerializer();
			if (s != null)
				Variables.yggdrasil.registerClassResolver(s);
		}

		EntityData.onRegistrationStop();
	}

	/**
	 * Sorts the class infos according to sub/superclasses and relations set with {@link ClassInfo#before(String...)} and {@link ClassInfo#after(String...)}.
	 */
	@SuppressFBWarnings("LI_LAZY_INIT_STATIC")
	private static void sortClassInfos() {
		assert classInfos == null;

		if (!Skript.testing() && SkriptConfig.addonSafetyChecks.value())
			removeNullElements();

		// merge before, after & sub/supertypes in after
		for (final ClassInfo<?> ci : tempClassInfos) {
			final Set<String> before = ci.before();
			if (before != null && !before.isEmpty()) {
				for (final ClassInfo<?> ci2 : tempClassInfos) {
					if (before.contains(ci2.getCodeName())) {
						ci2.after().add(ci.getCodeName());
						before.remove(ci2.getCodeName());
						if (before.isEmpty())
							break;
					}
				}
			}
		}
		for (final ClassInfo<?> ci : tempClassInfos) {
			for (final ClassInfo<?> ci2 : tempClassInfos) {
				if (ci == ci2)
					continue;
				if (ci.getC().isAssignableFrom(ci2.getC()))
					ci.after().add(ci2.getCodeName());
			}
		}

		// remove unresolvable dependencies (and print a warning if testing)
		for (final ClassInfo<?> ci : tempClassInfos) {
			final Set<String> s = new HashSet<>();
			final Set<String> before = ci.before();
			if (before != null) {
				for (final String b : before) {
					if (getClassInfoNoError(b) == null) {
						s.add(b);
					}
				}
				before.removeAll(s);
			}
			for (final String a : ci.after()) {
				if (getClassInfoNoError(a) == null) {
					s.add(a);
				}
			}
			ci.after().removeAll(s);
			if (!s.isEmpty() && Skript.testing())
				Skript.warning(s.size() + " dependency/ies could not be resolved for " + ci + ": " + StringUtils.join(s, ", "));
		}

		final List<ClassInfo<?>> classInfos = new ArrayList<>(tempClassInfos.size());

		boolean changed = true;
		while (changed) {
			changed = false;
			for (int i = 0; i < tempClassInfos.size(); i++) {
				final ClassInfo<?> ci = tempClassInfos.get(i);
				if (ci.after().isEmpty()) {
					classInfos.add(ci);
					tempClassInfos.remove(i);
					i--;
					for (final ClassInfo<?> ci2 : tempClassInfos)
						ci2.after().remove(ci.getCodeName());
					changed = true;
				}
			}
		}

		Classes.classInfos = classInfos.toArray(new ClassInfo[classInfos.size()]);

		// check for circular dependencies
		if (!tempClassInfos.isEmpty()) {
			final StringBuilder b = new StringBuilder();
			for (final ClassInfo<?> c : tempClassInfos) {
				if (b.length() != 0)
					b.append(", ");
				b.append(c.getCodeName() + " (after: " + StringUtils.join(c.after(), ", ") + ")");
			}
			throw new IllegalStateException("ClassInfos with circular dependencies detected: " + b.toString());
		}

		// debug message
		if (Skript.debug()) {
			final StringBuilder b = new StringBuilder();
			for (final ClassInfo<?> ci : classInfos) {
				if (b.length() != 0)
					b.append(", ");
				b.append(ci.getCodeName());
			}
			Skript.info("All registered classes in order: " + b.toString());
		}

	}

	@SuppressWarnings({"null", "unused"})
	private static void removeNullElements() {
		Iterator<ClassInfo<?>> it = tempClassInfos.iterator();
		while (it.hasNext()) {
			ClassInfo<?> ci = it.next();
			if (ci.getC() == null)
				it.remove();
		}
	}

	private static void checkAllowClassInfoInteraction() {
		if (Skript.isAcceptRegistrations())
			throw new IllegalStateException("Cannot use classinfos until registration is over");
	}

	/**
	 * Get a {@link List} of the {@link ClassInfo}s the {@code pattern} can be referenced to.
	 * @param pattern The {@link String} pattern.
	 */
	public static @Unmodifiable @Nullable List<ClassInfo<?>> getPatternInfos(String pattern) {
		pattern = pattern.toLowerCase(Locale.ENGLISH);
		List<ClassInfo<?>> infos = registeredLiteralPatterns.get(pattern);
		if (infos != null)
			return Collections.unmodifiableList(infos);
		return null;
	}

	@SuppressWarnings("null")
	public static List<ClassInfo<?>> getClassInfos() {
		checkAllowClassInfoInteraction();
		final ClassInfo<?>[] ci = classInfos;
		if (ci == null)
			return Collections.emptyList();
		return Collections.unmodifiableList(Arrays.asList(ci));
	}

	/**
	 * This method can be called even while Skript is loading.
	 *
	 * @param codeName
	 * @return The ClassInfo with the given code name
	 * @throws SkriptAPIException If the given class was not registered
	 */
	public static ClassInfo<?> getClassInfo(final String codeName) {
		final ClassInfo<?> ci = classInfosByCodeName.get(codeName);
		if (ci == null)
			throw new SkriptAPIException("No class info found for " + codeName);
		return ci;
	}

	/**
	 * This method can be called even while Skript is loading.
	 *
	 * @param codeName
	 * @return The class info registered with the given code name or null if the code name is invalid or not yet registered
	 */
	@Nullable
	public static ClassInfo<?> getClassInfoNoError(final @Nullable String codeName) {
		return classInfosByCodeName.get(codeName);
	}

	/**
	 * Gets the class info for the given class.
	 * <p>
	 * This method can be called even while Skript is loading.
	 *
	 * @param c The exact class to get the class info for.
	 * @return The class info for the given class or null if no info was found.
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T> ClassInfo<T> getExactClassInfo(final @Nullable Class<T> c) {
		return (ClassInfo<T>) exactClassInfos.get(c);
	}

	/**
	 * Gets the class info of the given class or its closest registered superclass. This method will never return null unless <tt>c</tt> is null.
	 *
	 * @param c
	 * @return The closest superclass's info
	 */
	@SuppressWarnings("unchecked")
	@Contract(pure = true, value = "!null -> !null")
	public static <T> ClassInfo<? super T> getSuperClassInfo(final Class<T> c) {
		assert c != null;
		ClassInfo<? super T> info = getExactClassInfo(c);
		if (info != null)
			return info;
		info = (ClassInfo<? super T>) superClassInfos.get(c);
		if (info != null)
			return info;
		for (final ClassInfo<?> ci : getClassInfos()) {
			if (ci.getC().isAssignableFrom(c)) {
				if (!Skript.isAcceptRegistrations())
					superClassInfos.put(c, ci);
				return (ClassInfo<? super T>) ci;
			}
		}
		assert false;
		return null;
	}

	/**
	 * Gets the class info of the super type of given classes or its closest registered superclass.
	 * This method is useful for Skript to avoid passing around "unknown" super types.
	 *
	 * @param classes The classes to determine a super type from.
	 * @return The closest info for the super type of <code>classes</code>.
	 */
	// This method is used to avoid issues like https://github.com/SkriptLang/Skript/issues/5848
	public static ClassInfo<?> getSuperClassInfo(Class<?>... classes) {
		return getSuperClassInfo(Utils.getSuperType(classes));
	}

	/**
	 * Gets all the class info of the given class in closest order to ending on object. This list will never be empty unless <tt>c</tt> is null.
	 *
	 * @param c the class to check if assignable from
	 * @return The closest list of superclass infos
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<ClassInfo<? super T>> getAllSuperClassInfos(Class<T> c) {
		assert c != null;
		checkAllowClassInfoInteraction();
		List<ClassInfo<? super T>> list = new ArrayList<>();
		for (ClassInfo<?> ci : getClassInfos()) {
			if (ci.getC().isAssignableFrom(c)) {
				list.add((ClassInfo<? super T>) ci);
			}
		}
		return list;
	}

	private static final Map<Property<?>, Set<ClassInfo<?>>> CLASS_INFOS_BY_PROPERTY = new HashMap<>();

	/**
	 * Mark a classinfo as having a property. Not for external use.
	 *
	 * @param property The property this classinfo has.
	 * @param classInfo The classinfo that has the property.
	 */
	@ApiStatus.Internal
	public static void hasProperty(@NotNull Property<?> property, @NotNull ClassInfo<?> classInfo) {
		Preconditions.checkNotNull(property, "property cannot be null");
		Preconditions.checkNotNull(classInfo, "classInfo cannot be null");
		CLASS_INFOS_BY_PROPERTY.computeIfAbsent(property, key -> new HashSet<>()).add(classInfo);
	}

	/**
	 * @param property The property the class infos must have.
	 * @return A list of all class infos with the given property.
	 */
	public static @NotNull Set<ClassInfo<?>> getClassInfosByProperty(@NotNull Property<?> property) {
		Preconditions.checkNotNull(property, "property cannot be null");
		return CLASS_INFOS_BY_PROPERTY.getOrDefault(property, Collections.emptySet());
	}

	/**
	 * Gets a class by its code name
	 *
	 * @param codeName
	 * @return the class with the given code name
	 * @throws SkriptAPIException If the given class was not registered
	 */
	public static Class<?> getClass(final String codeName) {
		checkAllowClassInfoInteraction();
		return getClassInfo(codeName).getC();
	}

	/**
	 * As the name implies
	 *
	 * @param name
	 * @return the class info or null if the name was not recognised
	 */
	@Nullable
	public static ClassInfo<?> getClassInfoFromUserInput(String name) {
		checkAllowClassInfoInteraction();
		name = "" + name.toLowerCase(Locale.ENGLISH);
		for (final ClassInfo<?> ci : getClassInfos()) {
			final Pattern[] uip = ci.getUserInputPatterns();
			if (uip == null)
				continue;
			for (final Pattern pattern : uip) {
				if (pattern.matcher(name).matches())
					return ci;
			}
		}
		return null;
	}

	/**
	 * As the name implies
	 *
	 * @param name
	 * @return the class or null if the name was not recognized
	 */
	@Nullable
	public static Class<?> getClassFromUserInput(final String name) {
		checkAllowClassInfoInteraction();
		final ClassInfo<?> ci = getClassInfoFromUserInput(name);
		return ci == null ? null : ci.getC();
	}

	/**
	 * Gets the default of a class
	 *
	 * @param codeName
	 * @return the expression holding the default value or null if this class doesn't have one
	 * @throws SkriptAPIException If the given class was not registered
	 */
	@Nullable
	public static DefaultExpression<?> getDefaultExpression(final String codeName) {
		checkAllowClassInfoInteraction();
		return getClassInfo(codeName).getDefaultExpression();
	}

	/**
	 * Gets the default expression of a class
	 *
	 * @param c The class
	 * @return The expression holding the default value or null if this class doesn't have one
	 */
	@Nullable
	public static <T> DefaultExpression<T> getDefaultExpression(final Class<T> c) {
		checkAllowClassInfoInteraction();
		final ClassInfo<T> ci = getExactClassInfo(c);
		return ci == null ? null : ci.getDefaultExpression();
	}

	/**
	 * Clones the given object by calling {@link ClassInfo#clone(Object)},
	 * getting the {@link ClassInfo} from the closest registered superclass
	 * (or the given object's class). Supports arrays too.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static Object clone(Object obj) {
		if (obj == null)
			return null;
		if (obj.getClass().isArray()) {
			int length = Array.getLength(obj);
			Object clone = Array.newInstance(obj.getClass().getComponentType(), length);
			for (int i = 0; i < length; i++) {
				Array.set(clone, i, clone(Array.get(obj, i)));
			}
			return clone;
		} else {
			ClassInfo classInfo = getSuperClassInfo(obj.getClass());
			return classInfo.clone(obj);
		}
	}

	/**
	 * Gets the name a class was registered with.
	 *
	 * @param c The exact class
	 * @return The name of the class or null if the given class wasn't registered.
	 */
	@Nullable
	public static String getExactClassName(final Class<?> c) {
		checkAllowClassInfoInteraction();
		final ClassInfo<?> ci = exactClassInfos.get(c);
		return ci == null ? null : ci.getCodeName();
	}

	/**
	 * Parses without trying to convert anything.
	 * <p>
	 * Can log an error xor other log messages.
	 *
	 * @param s
	 * @param c
	 * @return The parsed object
	 */
	@Nullable
	public static <T> T parseSimple(final String s, final Class<T> c, final ParseContext context) {
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			for (final ClassInfo<?> info : getClassInfos()) {
				final Parser<?> parser = info.getParser();
				if (parser == null || !parser.canParse(context) || !c.isAssignableFrom(info.getC()))
					continue;
				log.clear();
				@SuppressWarnings("unchecked")
				final T t = (T) parser.parse(s, context);
				if (t != null) {
					log.printLog();
					return t;
				}
			}
			log.printError();
		} finally {
			log.stop();
		}
		return null;
	}

	/**
	 * Parses a string to get an object of the desired type.
	 * <p>
	 * Instead of repeatedly calling this with the same class argument, you should get a parser with {@link #getParser(Class)} and use it for parsing.
	 * <p>
	 * Can log an error if it returned null.
	 *
	 * @param s The string to parse
	 * @param c The desired type. The returned value will be of this type or a subclass if it.
	 * @return The parsed object
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Nullable
	public static <T> T parse(final String s, final Class<T> c, final ParseContext context) {
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			T t = parseSimple(s, c, context);
			if (t != null) {
				log.printLog();
				return t;
			}
			for (final ConverterInfo<?, ?> conv : Converters.getConverterInfos()) {
				if ((context == ParseContext.COMMAND || context == ParseContext.PARSE) && (conv.getFlags() & Commands.CONVERTER_NO_COMMAND_ARGUMENTS) != 0)
					continue;
				if (c.isAssignableFrom(conv.getTo())) {
					log.clear();
					Object object = parseSimple(s, conv.getFrom(), context);
					if (object != null) {
						t = (T) ((Converter) conv.getConverter()).convert(object);
						if (t != null) {
							log.printLog();
							return t;
						}
					}
				}
			}
			log.printError();
		} finally {
			log.stop();
		}
		return null;
	}

	/**
	 * Gets a parser for parsing instances of the desired type from strings. The returned parser may only be used for parsing, i.e. you must not use its toString methods.
	 *
	 * @param to
	 * @return A parser to parse object of the desired type
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T> Parser<? extends T> getParser(final Class<T> to) {
		checkAllowClassInfoInteraction();
		final ClassInfo<?>[] classInfos = Classes.classInfos;
		if (classInfos == null)
			return null;
		for (int i = classInfos.length - 1; i >= 0; i--) {
			final ClassInfo<?> ci = classInfos[i];
			if (to.isAssignableFrom(ci.getC()) && ci.getParser() != null)
				return (Parser<? extends T>) ci.getParser();
		}
		for (final ConverterInfo<?, ?> conv : Converters.getConverterInfos()) {
			if (to.isAssignableFrom(conv.getTo())) {
				for (int i = classInfos.length - 1; i >= 0; i--) {
					final ClassInfo<?> ci = classInfos[i];
					final Parser<?> parser = ci.getParser();
					if (conv.getFrom().isAssignableFrom(ci.getC()) && parser != null)
						return Classes.createConvertedParser(parser, (Converter<?, ? extends T>) conv.getConverter());
				}
			}
		}
		return null;
	}

	/**
	 * Gets a parser for an exactly known class. You should usually use {@link #getParser(Class)} instead of this method.
	 * <p>
	 * The main benefit of this method is that it's the only class info method of Skript that can be used while Skript is initializing and thus useful for parsing configs.
	 *
	 * @param c
	 * @return A parser to parse object of the desired type
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T> Parser<? extends T> getExactParser(final Class<T> c) {
		if (Skript.isAcceptRegistrations()) {
			for (final ClassInfo<?> ci : tempClassInfos) {
				if (ci.getC() == c)
					return (Parser<? extends T>) ci.getParser();
			}
			return null;
		} else {
			final ClassInfo<T> ci = getExactClassInfo(c);
			return ci == null ? null : ci.getParser();
		}
	}

	private static <F, T> Parser<T> createConvertedParser(final Parser<?> parser, final Converter<F, T> converter) {
		return new Parser<T>() {
			@SuppressWarnings("unchecked")
			@Override
			@Nullable
			public T parse(final String s, final ParseContext context) {
				final Object f = parser.parse(s, context);
				if (f == null)
					return null;
				return converter.convert((F) f);
			}

			@Override
			public String toString(final T o, final int flags) {
				throw new UnsupportedOperationException();
			}

			@Override
			public String toVariableNameString(final T o) {
				throw new UnsupportedOperationException();
			}
        };
	}

	/**
	 * @param o Any object, preferably not an array: use {@link Classes#toString(Object[], boolean)} instead.
	 * @return String representation of the object (using a parser if found or {@link String#valueOf(Object)} otherwise).
	 * @see #toString(Object, StringMode)
	 * @see #toString(Object[], boolean)
	 * @see #toString(Object[], boolean, StringMode)
	 * @see Parser
	 */
	public static String toString(final @Nullable Object o) {
		return toString(o, StringMode.MESSAGE, 0);
	}

	public static String getDebugMessage(final @Nullable Object o) {
		return toString(o, StringMode.DEBUG, 0);
	}

	public static <T> String toString(final @Nullable T o, final StringMode mode) {
		return toString(o, mode, 0);
	}

	private static <T> String toString(final @Nullable T o, final StringMode mode, final int flags) {
		assert flags == 0 || mode == StringMode.MESSAGE;
		if (o == null)
			return Language.get("none");
		if (o.getClass().isArray()) {
			if (((Object[]) o).length == 0)
				return Language.get("none");
			final StringBuilder b = new StringBuilder();
			boolean first = true;
			for (final Object i : (Object[]) o) {
				if (!first)
					b.append(", ");
				b.append(toString(i, mode, flags));
				first = false;
			}
			return "[" + b.toString() + "]";
		}
		for (final ClassInfo<?> ci : getClassInfos()) {
			final Parser<?> parser = ci.getParser();
			if (parser != null && ci.getC().isInstance(o)) {
				@SuppressWarnings("unchecked")
				final String s = mode == StringMode.MESSAGE ? ((Parser<T>) parser).toString(o, flags)
						: mode == StringMode.DEBUG ? "[" + ci.getCodeName() + ":" + ((Parser<T>) parser).toString(o, mode) + "]"
								: ((Parser<T>) parser).toString(o, mode);
				return s;
			}
		}
		return mode == StringMode.VARIABLE_NAME ? "object:" + o : "" + o;
	}

	public static String toString(final Object[] os, final int flags, final boolean and) {
		return toString(os, and, null, StringMode.MESSAGE, flags);
	}

	public static String toString(final Object[] os, final int flags, final @Nullable ChatColor c) {
		return toString(os, true, c, StringMode.MESSAGE, flags);
	}

	public static String toString(final Object[] os, final boolean and) {
		return toString(os, and, null, StringMode.MESSAGE, 0);
	}

	public static String toString(final Object[] os, final boolean and, final StringMode mode) {
		return toString(os, and, null, mode, 0);
	}

	private static String toString(final Object[] os, final boolean and, final @Nullable ChatColor c, final StringMode mode, final int flags) {
		if (os.length == 0)
			return toString(null);
		if (os.length == 1)
			return toString(os[0], mode, flags);
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < os.length; i++) {
			if (i != 0) {
				if (c != null)
					b.append(c.toString());
				if (i == os.length - 1)
					b.append(and ? " and " : " or ");
				else
					b.append(", ");
			}
			b.append(toString(os[i], mode, flags));
		}
		return "" + b.toString();
	}

	/**
	 * consists of {@link Yggdrasil#MAGIC_NUMBER} and {@link Variables#YGGDRASIL_VERSION}
	 */
	private final static byte[] YGGDRASIL_START = {(byte) 'Y', (byte) 'g', (byte) 'g', 0, (Variables.YGGDRASIL_VERSION >>> 8) & 0xFF, Variables.YGGDRASIL_VERSION & 0xFF};

	@SuppressWarnings("null")
	private final static Charset UTF_8 = Charset.forName("UTF-8");

	private static byte[] getYggdrasilStart(final ClassInfo<?> c) throws NotSerializableException {
		assert Enum.class.isAssignableFrom(Kleenean.class) && Tag.getType(Kleenean.class) == Tag.T_ENUM : Tag.getType(Kleenean.class); // TODO why is this check here?
		final Tag t = Tag.getType(c.getC());
		assert t.isWrapper() || t == Tag.T_STRING || t == Tag.T_OBJECT || t == Tag.T_ENUM;
		final byte[] cn = t == Tag.T_OBJECT || t == Tag.T_ENUM ? Variables.yggdrasil.getID(c.getC()).getBytes(UTF_8) : null;
		final byte[] r = new byte[YGGDRASIL_START.length + 1 + (cn == null ? 0 : 1 + cn.length)];
		int i = 0;
		for (; i < YGGDRASIL_START.length; i++)
			r[i] = YGGDRASIL_START[i];
		r[i++] = t.tag;
		if (cn != null) {
			r[i++] = (byte) cn.length;
			for (int j = 0; j < cn.length; j++)
				r[i++] = cn[j];
		}
		assert i == r.length;
		return r;
	}

	/**
	 * Represents a context for serialization of a value as a variable.
	 *
	 * @param classInfo class info of the object
	 * @param value object to serialize
	 */
	private record SerializationContext(ClassInfo<?> classInfo, Object value) {
		public @Nullable Serializer<?> serializer() {
			return classInfo.getSerializer();
		}
		public boolean mustSyncDeserialization() {
			Serializer<?> serializer = serializer();
			return serializer != null && serializer.mustSyncDeserialization();
		}
	}

	/**
	 * Returns the serializer used for serializing the given object as a variable.
	 * <p>
	 * Returns {@code null} if the object can not be serialized (there is no serializer available).
	 *
	 * @param object object to serialize
	 * @return serializer for the serialization of given object
	 */
	private static SerializationContext getSerializationContext(Object object) {
		ClassInfo<?> classInfo = getSuperClassInfo(object.getClass());

		if (classInfo.getSerializeAs() != null) {
			classInfo = getExactClassInfo(classInfo.getSerializeAs());
			if (classInfo == null) {
				assert false : object.getClass();
				return null;
			}
			object = Converters.convert(object, classInfo.getC());
			if (object == null) {
				assert false : classInfo.getCodeName();
				return null;
			}
		}
		return new SerializationContext(classInfo, object);
	}

	/**
	 * Serializes the provided map of variables.
	 * <p>
	 * Is blocking if the serializer for some of the variables needs to be synchronized and
	 * the method is not called from the main thread.
	 * <p>
	 * This does processed null values in the map and will provide empty
	 * serialized variables in the returned set for such variables.
	 * <p>
	 * This method is thread safe.
	 *
	 * @param variables variables to serialize
	 * @return serialized variables, returns null if the serialization failed
	 * because Skript is disabled, some of the variables need to be serialized
	 * on the main thread and this method was called off the main thread.
	 */
	@Blocking
	public static @Nullable Set<SerializedVariable> serialize(Map<String, @Nullable Object> variables) {
		Set<SerializedVariable> collected = ConcurrentHashMap.newKeySet();
		Map<String, SerializationContext> needsSync = new ConcurrentHashMap<>();

		variables.entrySet().parallelStream().forEach(entry -> {
			String key = entry.getKey();
			Object value = entry.getValue();

			if (value == null) {
				collected.add(new SerializedVariable(key, null));
				return;
			}

			SerializationContext context = getSerializationContext(value);
			assert context != null;
			if (context.classInfo.getSerializer() == null) {
				collected.add(new SerializedVariable(key, null));
				return;
			}

			if (context.mustSyncDeserialization()) {
				needsSync.put(key, context);
				return;
			}
			try {
				var serialized = serialize(context.value, context.classInfo);
				collected.add(new SerializedVariable(key, serialized));
			} catch (IOException exception) {
				Skript.error("Failed to serialize " + context.value);
			}
		});

		Runnable syncSerialization = () -> needsSync.forEach((key, context) -> {
			try {
				var serialized = serialize(context.value, context.classInfo);
				collected.add(new SerializedVariable(key, serialized));
			} catch (IOException exception) {
				Skript.exception(exception, "Failed to serialize " + context.value);
			}
		});

		if (needsSync.isEmpty())
			return collected;

		if (Bukkit.isPrimaryThread()) {
			syncSerialization.run();
		} else {
			try {
				if (!Skript.getInstance().isEnabled())
					// At this point we can not serialize variables synchronously,
					// we fail rather than provide partial result
					return null;
				CompletableFuture.supplyAsync(() -> {
					syncSerialization.run();
					return null;
				}, Bukkit.getScheduler().getMainThreadExecutor(Skript.getInstance())).get();
			} catch (Exception exception) {
				Skript.exception(exception, "Failed to serialize variables on the main thread");
			}
		}

		return collected;
	}

	/**
	 * Serializes the provided object to a value for a variable.
	 * <p>
	 * Is blocking if the serializer for the action needs to be synchronized and
	 * the method is not called from the main thread.
	 * <p>
	 * This method is thread safe.
	 *
	 * @param object object to serialize
	 * @return serialized value of null if no serializer is available
	 */
	@Blocking
	public static SerializedVariable.@Nullable Value serialize(@Nullable Object object) {
		if (object == null)
			return null;
		var result = serialize(Map.of("object", object));
		if (result == null)
			return null;
		var iterator = result.iterator();
		if (!iterator.hasNext())
			return null;
		return iterator.next().value();
	}

	/**
	 * The serialization process for a single object.
	 * <p>
	 * This method must be called from the main thread if the serializer for
	 * given class info must be synchronized.
	 *
	 * @see #serialize(Object)
	 * @see #serialize(Map)
	 */
	private static SerializedVariable.Value serialize(Object object, ClassInfo<?> classInfo) throws IOException {
		assert classInfo.getSerializer() != null;
		assert !classInfo.getSerializer().mustSyncDeserialization() || Bukkit.isPrimaryThread();

		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
		YggdrasilOutputStream yggdrasilOutputStream = Variables.yggdrasil.newOutputStream(byteOutputStream);

		yggdrasilOutputStream.writeObject(object);
		yggdrasilOutputStream.flush();
		yggdrasilOutputStream.close();

		byte[] byteArray = byteOutputStream.toByteArray();
		byte[] start = getYggdrasilStart(classInfo);
		for (int i = 0; i < start.length; i++)
			assert byteArray[i] == start[i] : object + " (" + classInfo.getC().getName() + "); " + Arrays.toString(start) + ", " + Arrays.toString(byteArray);
		byte[] byteArrayCopy = new byte[byteArray.length - start.length];
		System.arraycopy(byteArray, start.length, byteArrayCopy, 0, byteArrayCopy.length);
		return new SerializedVariable.Value(classInfo.getCodeName(), byteArrayCopy);
	}

	/**
	 * Deserializes the provided set of serialized variables.
	 * <p>
	 * Is blocking if the serializer for some of the variables needs to be synchronized and
	 * the method is not called from the main thread.
	 * <p>
	 * This does skip empty variables and does not map them in the returned map to null values.
	 * <p>
	 * This method is thread safe.
	 *
	 * @param variables variables to deserialize
	 * @return deserialized variables, returns null if the deserialization failed
	 * because Skript is disabled, some of the variables need to be deserialization
	 * on the main thread and this method was called off the main thread.
	 */
	@Blocking
	public static @Nullable Map<String, Object> deserialize(Set<SerializedVariable> variables) {
		Map<String, Object> collected = new ConcurrentHashMap<>();
		Set<SerializedVariable> needsSync = ConcurrentHashMap.newKeySet();

		variables.stream().parallel().forEach(var -> {
			String key = var.name();
			SerializedVariable.Value value = var.value();

			if (value == null)
				return;

			ClassInfo<?> classInfo = getClassInfoNoError(value.type());
			if (classInfo == null) {
				collected.put(key, null);
				return;
			}

			Serializer<?> serializer = classInfo.getSerializer();
			if (serializer == null) {
				collected.put(key, null);
				return;
			}

			if (serializer.mustSyncDeserialization()) {
				needsSync.add(var);
				return;
			}

			collected.put(key, deserialize(new ByteArrayInputStream(value.data()), classInfo));
		});

		Runnable syncDeserialization = () -> needsSync.forEach(var -> {
			String key = var.name();
			SerializedVariable.Value value = var.value();
			assert value != null;
			ClassInfo<?> classInfo = getClassInfoNoError(value.type());
			var deserialized = deserialize(new ByteArrayInputStream(value.data()), classInfo);
			collected.put(key, deserialized);
		});

		if (needsSync.isEmpty())
			return collected;

		if (Bukkit.isPrimaryThread()) {
			syncDeserialization.run();
		} else {
			if (!Skript.getInstance().isEnabled())
				// At this point we can not deserialize variables synchronously,
				// we fail rather than provide partial result
				return null;
			try {
				CompletableFuture.supplyAsync(() -> {
					syncDeserialization.run();
					return null;
				}, Bukkit.getScheduler().getMainThreadExecutor(Skript.getInstance())).get();
			} catch (Exception exception) {
				Skript.exception(exception, "Failed to serialize variables on the main thread");
			}
		}

		return collected;
	}

	/**
	 * Deserializes the provided variable value to an object.
	 * <p>
	 * Is blocking if the serializer for the action needs to be synchronized and
	 * the method is not called from the main thread.
	 * <p>
	 * This method is thread safe.
	 *
	 * @param value value to deserialize
	 * @return deserialized object of null if no serializer is available
	 */
	@Blocking
	public static @Nullable Object deserialize(SerializedVariable. @Nullable Value value) {
		if (value == null)
			return null;
		var result = deserialize(Set.of(new SerializedVariable("", value)));
		if (result == null || result.isEmpty())
			return null;
		return result.values().iterator().next();
	}

	/**
	 * Deserializes the provided variable value to an object.
	 * <p>
	 * Is blocking if the serializer for the action needs to be synchronized and
	 * the method is not called from the main thread.
	 * <p>
	 * This method is thread safe.
	 *
	 * @param value value to deserialize
	 * @param type type of the value
	 * @return deserialized object of null if no serializer is available
	 */
	@Blocking
	public static @Nullable Object deserialize(byte[] value, ClassInfo<?> type) {
		return deserialize(value, type.getCodeName());
	}

	/**
	 * Deserializes the provided variable value to an object.
	 * <p>
	 * Is blocking if the serializer for the action needs to be synchronized and
	 * the method is not called from the main thread.
	 * <p>
	 * This method is thread safe.
	 *
	 * @param value value to deserialize
	 * @param type type of the value
	 * @return deserialized object of null if no serializer is available
	 */
	@Blocking
	public static @Nullable Object deserialize(byte[] value, String type) {
		return deserialize(new SerializedVariable.Value(type, value));
	}

	/**
	 * The deserialization process for a single object.
	 * <p>
	 * This method must be called from the main thread if the serializer for
	 * given class info must be synchronized.
	 *
	 * @see #deserialize(byte[], ClassInfo)
	 * @see #deserialize(Set)
	 */
	private static @Nullable Object deserialize(InputStream inputStream, ClassInfo<?> classInfo) {
		assert classInfo.getSerializer() != null;
		assert !classInfo.getSerializer().mustSyncDeserialization() || Bukkit.isPrimaryThread();

		try (var sis = new SequenceInputStream(new ByteArrayInputStream(getYggdrasilStart(classInfo)), inputStream);
			 var in = Variables.yggdrasil.newInputStream(sis)) {
			return in.readObject();
		} catch (IOException exception) {
			if (Skript.testing())
				Skript.exception(exception, "Failed to deserialize variable of type " + classInfo.getCodeName());
			return null;
		}
	}

	/**
	 * Deserializes an object.
	 * <p>
	 * This method must only be called from Bukkit main thread!
	 *
	 * @param type type of the value
	 * @param value value as a string
	 * @return deserialized value or null if the input is invalid
	 * @deprecated for legacy deserialization, use {@link #deserialize(byte[], String)}
	 */
	@SuppressWarnings("removal")
	@Deprecated(since = "2.3.0", forRemoval = true)
	public static @Nullable Object deserialize(final String type, final String value) {
		assert Bukkit.isPrimaryThread();
		final ClassInfo<?> ci = getClassInfoNoError(type);
		if (ci == null)
			return null;
		final Serializer<?> s = ci.getSerializer();
		if (s == null)
			return null;
		return s.deserialize(value);
	}

}
