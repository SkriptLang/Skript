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
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Name("Copy Into Variable")
@Description({
	"Copies objects into a variable. When copying a list over to another list, the source list and its sublists are also copied over.",
	"<strong>Note: Copying a value into a variable/list will overwrite the existing data.</strong>"
})
@Examples({
	"set {_foo::bar} to 1",
	"set {_foo::sublist::foobar} to \"hey\"",
	"copy {_foo::*} to {_copy::*}",
	"broadcast indices of {_copy::*} # bar, sublist",
	"broadcast {_copy::bar} # 1",
	"broadcast {_copy::sublist::foobar} # \"hey!\""
})
@Since("2.8.0")
@Keywords({"clone", "variable", "list"})
public class EffCopy extends Effect {

	static {
		Skript.registerEffect(EffCopy.class, "copy %~objects% [in]to %~objects%");
	}

	private Expression<?> source;
	private Expression<?> rawDestination;
	private List<Variable<?>> destinations;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		source = exprs[0];
		rawDestination = exprs[1];
		if (exprs[1] instanceof Variable<?>) {
			destinations = Collections.singletonList((Variable<?>) exprs[1]);
		} else if (exprs[1] instanceof ExpressionList<?>) {
			destinations = unwrapExpressionList((ExpressionList<?>) exprs[1]);
		}
		if (destinations == null) {
			Skript.error("You can only copy objects into variables");
			return false;
		}
		for (Variable<?> destination : destinations) {
			if (!source.isSingle() && destination.isSingle()) {
				Skript.error("Cannot copy multiple objects into a single variable");
				return false;
			}
		}
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void execute(Event event) {
		if (!(source instanceof Variable) || source.isSingle()) {
			ChangeMode mode = ChangeMode.SET;
			Object[] clone = (Object[]) Classes.clone(source.getArray(event));
			if (clone.length == 0)
				mode = ChangeMode.DELETE;
            for (Variable<?> dest : destinations)
                dest.change(event, clone, mode);
            return;
		}

		Map<String, Object> source = copyMap((Map<String, Object>) ((Variable<?>) this.source).getRaw(event));

		// If we're copying {_foo::*} we don't want to also copy {_foo}
		if (source != null)
			source.remove(null);

		for (Variable<?> destination : destinations) {
			destination.change(event, null, ChangeMode.DELETE);
			if (source == null)
				continue;
	
			String target = destination.getName().getSingle(event);
			target = target.substring(0, target.length() - (Variable.SEPARATOR + "*").length());
			set(event, target, source, destination.isLocal());
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "copy " + source.toString(event, debug) + " into " + rawDestination.toString(event, debug);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private static Map<String, Object> copyMap(@Nullable Map<String, Object> map) {
		if (map == null)
			return null;
		Map<String, Object> copy = new HashMap<>(map.size());
		map.forEach((key, value) -> {
            if (value instanceof Map) {
                copy.put(key, copyMap((Map<String, Object>) value));
				return;
			}
            copy.put(key, Classes.clone(value));
        });
		return copy;
	}

	@SuppressWarnings("unchecked")
	private static void set(Event event, String targetName, Map<String, Object> source, boolean local) {
		source.forEach((key, value) -> {
			String node = targetName + (key == null ? "" : Variable.SEPARATOR + key);
			if (value instanceof Map) {
				set(event, node, (Map<String, Object>) value, local);
				return;
			}
			Variables.setVariable(node, value, event, local);
		});
	}

	private static List<Variable<?>> unwrapExpressionList(ExpressionList<?> expressionList) {
		Expression<?>[] expressions = expressionList.getExpressions();
		List<Variable<?>> destinations = new ArrayList<>();
        for (Expression<?> expression : expressions) {
            if (expression instanceof Variable<?>) {
                destinations.add((Variable<?>) expression);
                continue;
            }
            if (!(expression instanceof ExpressionList<?>))
                return null;
            destinations.addAll(unwrapExpressionList((ExpressionList<?>) expression));
        }
		return destinations;
	}

}
