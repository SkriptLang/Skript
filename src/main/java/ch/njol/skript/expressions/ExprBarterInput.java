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
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.jetbrains.annotations.Nullable;

@Name("Barter Input")
@Description("The item picked up by the piglin in a piglin bartering event.")
@Examples({
	"on piglin barter:",
		"\tif the bartering input is a gold ingot:",
			"\t\tbroadcast \"my precious...\""
})
@Since("INSERT VERSION")
public class ExprBarterInput extends SimpleExpression<ItemType> {

	static {
		if (Skript.classExists("org.bukkit.event.entity.PiglinBarterEvent")) {
			Skript.registerExpression(ExprBarterInput.class, ItemType.class,
					ExpressionType.SIMPLE, "[the] [piglin] barter[ing] input");
		}
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult result) {
		if (!getParser().isCurrentEvent(PiglinBarterEvent.class)) {
			Skript.error("The expression 'barter input' can only be used in the piglin bartering event");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected ItemType[] get(Event event) {
		if (!(event instanceof PiglinBarterEvent))
			return null;

		return new ItemType[] { new ItemType(((PiglinBarterEvent) event).getInput()) };
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the barter input";
	}

}
