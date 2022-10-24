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
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.advancement.Advancement;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("All Advancements")
@Description("All of the registered advancements.")
@Examples("add all advancements to advancements of player")
@Since("INSERT VERSION")
public class ExprAllAdvancements extends SimpleExpression<Advancement> {

	static {
		Skript.registerExpression(ExprAllAdvancements.class, Advancement.class, ExpressionType.SIMPLE, "all [[of] the] advancements");
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "all advancements";
	}

	@Override
	public boolean isSingle() {
		return Utils.getAllAdvancements().length == 1;
	}

	@Override
	public Class<? extends Advancement> getReturnType() {
		return Advancement.class;
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		return true;
	}

	@Override
	protected @Nullable Advancement[] get(Event e) {
		return Utils.getAllAdvancements();
	}
}
