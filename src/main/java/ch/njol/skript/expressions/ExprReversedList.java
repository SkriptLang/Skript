/*
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
 * 
 * Copyright 2011-2017 Peter Güttinger and contributors
 * 
 */

package ch.njol.skript.expressions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;

@Name("Reversed List")
@Description("Reverses the given list.")
@Examples("set {_list::*} to reversed {_list::*}")
@Since("INSERT VERSION")
public class ExprReversedList extends SimpleExpression<Object> {
	
	static {
		Skript.registerExpression(ExprReversedList.class, Object.class, ExpressionType.COMBINED, "(reverse(d| of)|flip[ped]) %objects%");
	}
	
	@SuppressWarnings("null")
	private Expression<?> list;
	
	@Override
	public Class<? extends Object> getReturnType() {
		return list.getReturnType();
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@SuppressWarnings("null")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		list = LiteralUtils.defendExpression(exprs[0]);
		return LiteralUtils.canInitSafely(list);
	}
	
	@Override
	@Nullable
	protected Object[] get(Event event) {
		Object[] origin = list.getArray(event);
		List<Object> reversed = Arrays.asList(origin.clone());
		Collections.reverse(reversed);
		return reversed.toArray();
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "reversed list: " + list.toString(e, debug);
	}

}