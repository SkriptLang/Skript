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
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Inline While")
@Description("A shorthand effect for executing an effect while a condition is met.")
@Examples({"on join:",
	"\tadd (random integer between 1 and 5) to {_amount} while chance of 50%",
	"\tgive {_amount} of dirt to player"})
@Since("INSERT VERSION")
public class EffWhile extends Effect {
	
	static {
		Skript.registerEffect(EffWhile.class, "do [effect] <.+> [(1:((now|once) [and] [then]))] while <.+>");
	}
	
	private Effect effect;
	private Condition condition;
	
	private boolean doWhile;
	private boolean ranDoWhile = false;
	
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		String rawEffect = parseResult.regexes.get(0).group();
		String rawCondition = parseResult.regexes.get(1).group();
		
		effect = Effect.parse(rawEffect, "Can't understand this effect: " + rawEffect);
		if (effect instanceof EffLoop || effect instanceof EffWhile) {
			Skript.error("Inline while may not be nested inside another inline iterator.");
			return false;
		}
		condition = Condition.parse(rawCondition, "Can't understand this condition: " + rawCondition);
		doWhile = parseResult.mark == 1;
		
		return effect != null && condition != null;
	}
	
	@Override
	protected void execute(Event event) {}
	
	@Override
	public @Nullable TriggerItem walk(Event event) {
		if (doWhile && !ranDoWhile) {
			debug(event, true);
			ranDoWhile = true;
			effect.setNext(this);
			return effect;
		} else if (condition.check(event)) {
			debug(event, true);
			effect.setNext(this);
			return effect;
		} else {
			debug(event, false);
			effect.setNext(getNext());
			return getNext();
		}
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (doWhile ? "do " : "") + effect.toString(event, debug) + " while " + condition.toString(event, debug);
	}

}
