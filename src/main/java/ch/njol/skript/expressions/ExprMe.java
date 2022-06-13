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

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.command.EffectCommandEvent;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Me")
@Description("A 'me' expression that can be used in effect commands only.")
@Examples({"!heal me", "!kick myself", "!give a diamond axe to me"})
@Since("2.1.1")
public class ExprMe extends SimpleExpression<CommandSender> {
	static {
		Skript.registerExpression(ExprMe.class, CommandSender.class, ExpressionType.SIMPLE, "me", "my[self]");
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		if (!getParser().isCurrentEvent(EffectCommandEvent.class)) {
			return false;
		}
		return true;
	}
	
	@Override
	@Nullable
	protected CommandSender[] get(final Event e) {
		if (e instanceof EffectCommandEvent)
			return new CommandSender[] {((EffectCommandEvent) e).getSender()};
		return new CommandSender[0];
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends CommandSender> getReturnType() {
		return CommandSender.class;
	}
	
	@Override
	public String toString(@Nullable final Event e, final boolean debug) {
		return "me";
	}
	
}
