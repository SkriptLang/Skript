package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.NonNullIterator;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * A literal which has yet to be parsed. This is returned if %object(s)% is used within patterns and no expression matches.
 *
 * @see SimpleLiteral
 */
public class UnparsedLiteral implements Literal<Object> {

	private final String data;
	private final @Nullable LogEntry error;
	private final @Nullable List<ClassInfo<?>> possibleInfos;
	private boolean reparsed = false;
	private boolean converted = false;

	/**
	 * @param data non-null, non-empty & trimmed string
	 */
	public UnparsedLiteral(String data) {
		this(data, null);
	}

	/**
	 * @param data non-null, non-empty & trimmed string
	 * @param error Error to log if this literal cannot be parsed
	 */
	public UnparsedLiteral(String data, @Nullable LogEntry error) {
		assert data.length() > 0;
		assert error == null || error.getLevel() == Level.SEVERE;
		this.data = data;
		this.error = error;
		this.possibleInfos = Classes.getPatternInfos(data);
	}

	public String getData() {
		return data;
	}

	@Override
	public Class<?> getReturnType() {
		return Object.class;
	}

	@Override
	public <R> @Nullable Literal<? extends R> getConvertedExpression(Class<R>... to) {
		return getConvertedExpression(ParseContext.DEFAULT, to);
	}

	public <R> @Nullable Literal<? extends R> getConvertedExpression(ParseContext context, Class<? extends R>... to) {
		assert to.length > 0;
		assert to.length == 1 || !CollectionUtils.contains(to, Object.class);
		ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			for (Class<? extends R> type : to) {
				assert type != null;
				R parsedObject = Classes.parse(data, type, context);
				if (parsedObject != null) {
					if (!type.equals(Object.class))
						converted = true;
					log.printLog();
					return new SimpleLiteral<>(parsedObject, false, this);
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
	public String toString(@Nullable Event event, boolean debug) {
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

	public <T> @Nullable SimpleLiteral<T> reparse(Class<T> type) {
		T typedObject = Classes.parse(data, type, ParseContext.DEFAULT);
		if (typedObject != null) {
			if (!type.equals(Object.class))
				reparsed = true;
			return new SimpleLiteral<T>(typedObject, false, new UnparsedLiteral(data));
		}
		return null;
	}

	/**
	 * Check if this {@link UnparsedLiteral} was successfully reparsed via {@link #reparse(Class)}.
	 * @return {@code True} if successfully reparsed.
	 */
	public boolean wasReparsed() {
		return reparsed;
	}

	/**
	 * Check if this {@link UnparsedLiteral} was successfully converted via {@link #getConvertedExpression(ParseContext, Class[])}.
	 * @return {@code True} if successfully converted.
	 */
	public boolean wasConverted() {
		return converted;
	}

	/**
	 * Get a {@link List} of all possible {@link ClassInfo}s this {@link UnparsedLiteral} can be parsed as.
	 */
	public @Nullable List<ClassInfo<?>> getPossibleInfos() {
		return possibleInfos;
	}

	/**
	 * Print a warning of this {@link UnparsedLiteral} being able to be referenced to multiple {@link ClassInfo}s.
	 * Will print warning if this {@link UnparsedLiteral} was not successfully reparsed and converted and {@link #possibleInfos}
	 * has multiple {@link ClassInfo}s.
	 * @return {@code True} if the warning was printed.
	 */
	public boolean multipleWarning() {
		if (reparsed || converted || possibleInfos == null || possibleInfos.size() <= 1)
			return false;
		String infoCodeName = possibleInfos.get(0).getName().getSingular();
		String combinedInfos = Classes.toString(possibleInfos.toArray(), true);
		Skript.warning("'" +  data + "' has multiple types (" + combinedInfos + "). Consider specifying which type to use: '"
			+ data + " (" + infoCodeName + ")'");
		return true;
	}

	private static SkriptAPIException invalidAccessException() {
		return new SkriptAPIException("UnparsedLiterals must be converted before use");
	}

	@Override
	public Object[] getAll() {
		throw invalidAccessException();
	}

	@Override
	public Object[] getAll(Event event) {
		throw invalidAccessException();
	}

	@Override
	public Object[] getArray() {
		throw invalidAccessException();
	}

	@Override
	public Object[] getArray(Event event) {
		throw invalidAccessException();
	}

	@Override
	public Object getSingle() {
		throw invalidAccessException();
	}

	@Override
	public Object getSingle(Event event) {
		throw invalidAccessException();
	}

	@Override
	public NonNullIterator<Object> iterator(Event event) {
		throw invalidAccessException();
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) throws UnsupportedOperationException {
		throw invalidAccessException();
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		throw invalidAccessException();
	}

	@Override
	public boolean check(Event event, Predicate<? super Object> checker) {
		throw invalidAccessException();
	}

	@Override
	public boolean check(Event event, Predicate<? super Object> checker, boolean negated) {
		throw invalidAccessException();
	}

	@Override
	public boolean setTime(int time) {
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
	public boolean isLoopOf(String input) {
		throw invalidAccessException();
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		throw invalidAccessException();
	}

}
