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
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.sections.SecSwitch;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.experiment.Feature;

@Name("Switch Subject (Experimental)")
@Description({
	"The subject of a switch section (the thing being checked).",
	"This is usable within the cases/conditions of the switch section.",
	"See 'Switch Section' for more details."
})
@Examples({
	"switch {_words::*}:",
	"\tit is \"hello\":",
	"\t\tbroadcast \"hello!\"",
	"\tit is \"goodbye\":",
	"\t\tbroadcast \"see you later!\""
})
@Since("INSERT VERSION")
public class ExprIt extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprIt.class, Object.class, ExpressionType.SIMPLE, "this", "it");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed,
                        SkriptParser.ParseResult parseResult) {
		if (!this.hasExperiment(Feature.SWITCH_SECTIONS))
			return false;
		return SecSwitch.isInSwitch(this);
	}

	@Override
	protected @Nullable Object[] get(Event event) {
		return new Object[] {SecSwitch.current(event)};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		try {
			return SecSwitch.getSwitch(this).getSubjectType();
		} catch (IllegalStateException | ClassCastException | NullPointerException ex) {
			/*
			In case this is (somehow) used in a time or place where the parser doesn't know
			about its outer switch yet, the switch hasn't been added to the trigger sections
			or the switch hasn't been initialised.
			Conceivably, this could happen if this is asked for by a condition and SecSwitch
			hasn't run its `init` yet, so we don't know what the subject is.
			 */
			return Object.class;
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "this";
	}

}
