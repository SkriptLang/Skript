package ch.njol.skript.lang.util;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Container;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Peter Güttinger
 */
public class ContainerExpression extends SimpleExpression<Object> {

	final Expression<? extends Container<?>> expr;
	private final Class<?> type;

	public ContainerExpression(Expression<? extends Container<?>> expr, Class<?> type) {
		this.expr = expr;
		this.type = type;
	}

	@Override
	protected Object[] get(Event e) {
		throw new UnsupportedOperationException("ContainerExpression must only be used by Loops");
	}

	@Override
	public @Nullable Iterator<Object> iterator(Event event) {
		Iterator<? extends Container<?>> iterator = expr.iterator(event);
		if (iterator == null)
			return null;
		return new Iterator<>() {
			private @Nullable Iterator<?> current;

			@Override
			public boolean hasNext() {
				Iterator<?> current = this.current;
				while (iterator.hasNext() && (current == null || !current.hasNext())) {
					this.current = current = iterator.next().containerIterator();
				}
				return current != null && current.hasNext();
			}

			@Override
			public Object next() {
				if (!hasNext())
					throw new NoSuchElementException();
				Iterator<?> current = this.current;
				if (current == null)
					throw new NoSuchElementException();
				Object value = current.next();
				assert value != null : this.current + "; " + expr;
				return value;
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
	public Class<?> getReturnType() {
		return type;
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return expr.toString(event, debug);
	}

}
