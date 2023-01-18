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
package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Loopable;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.LongStream;

@Name("Inline Loop")
@Description("A shorthand effect for iterating over objects or repeating X times.")
@Examples({"command /uuids:",
	"/ttrigger:",
	"\t\tdo add uuid of loop-player to {_uuids::*} for every element in all players",
	"\t\tsend (join {_uuids::*} by \", \") to player",
	
	"on join:",
	"\tdo send \"Hello, world!\" to player 5 times"
})
@Since("INSERT VERSION")
public class EffLoop extends Effect implements Loopable {
	
	static {
		Skript.registerEffect(EffLoop.class, "do [effect] <.+> %number% times", "do [effect] <.+> for [(each|every) (element|item|entry) (in|of)] %objects%");
	}
	
	private Effect effect;
	private @Nullable Expression<Number> times;
	private @Nullable Expression<Object> iterable;
	
	private boolean usesTimes;
	
	private final Map<Event, Object> current = new WeakHashMap<>();
	private final Map<Event, Iterator<?>> currentIter = new WeakHashMap<>();
	
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		String rawEffect = parseResult.regexes.get(0).group();
		
		usesTimes = matchedPattern == 0;
		if (usesTimes) {
			times = LiteralUtils.defendExpression(expressions[0]);
			if (!LiteralUtils.canInitSafely(times)) {
				Skript.error("Can't understand this loop: '" + parseResult.expr + "'");
				return false;
			}
		} else {
			iterable = LiteralUtils.defendExpression(expressions[0]);
			if (!LiteralUtils.canInitSafely(iterable)) {
				Skript.error("Can't understand this loop: '" + parseResult.expr + "'");
				return false;
			}
		}
		
		try {
			getParser().pushLoop(this);
			effect = Effect.parse(rawEffect, "Can't understand this effect: " + rawEffect);
		} finally {
			getParser().popLoop();
		}
		if (effect instanceof EffLoop || effect instanceof EffWhile) {
			Skript.error("Inline loop may not be nested inside another inline iterator.");
			return false;
		}
		
		return effect != null && (times != null || iterable != null);
	}
	
	@Override
	protected void execute(Event event) {}
	
	@Override
	public @Nullable TriggerItem walk(Event event) {
		Iterator<?> iterator;
		
		if (usesTimes) {
			assert times != null;
			
			iterator = currentIter.computeIfAbsent(event,
				e -> LongStream.range(1, times.getSingle(event).longValue() + 1).iterator());
		} else {
			assert iterable != null;
			
			iterator = currentIter.computeIfAbsent(event, e -> iterable instanceof Variable
				? ((Variable<?>) iterable).variablesIterator(e)
				: iterable.iterator(e));
		}
		
		if (iterator != null && iterator.hasNext()) {
			debug(event, true);
			current.put(event, iterator.next());
			effect.setNext(this);
			return effect;
		} else {
			debug(event, false);
			exit(event);
			effect.setNext(getNext());
			return getNext();
		}
	}
	
	@Override
	public @Nullable Object getCurrent(Event e) {
		return current.get(e);
	}
	
	@Override
	public @NotNull Expression<?> getLoopedExpression() {
		return usesTimes ? times : iterable;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "do " + effect.toString(event, debug) + " " + (usesTimes
			? times.toString(event, debug) + " times"
			: "for each element in " + iterable.toString(event, debug));
	}
	
	private void exit(Event event) {
		current.remove(event);
		currentIter.remove(event);
	}
	
}
