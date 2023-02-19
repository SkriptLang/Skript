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
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

@Name("Readied Arrow/Bow")
@Description("The bow or arrow is a Ready Arrow event.")
@Examples({
	"on player ready arrow:",
		"\tselected bow's name is \"Spectral Bow\"",
		"\tif selected arrow is not a spectral arrow:",
			"\t\tcancel event"
})
@Since("INSERT VERSION")
@Events("player ready arrow")
public class ExprReadiedArrow extends SimpleExpression<ItemType> {

	static {
		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerReadyArrowEvent"))
			Skript.registerExpression(ExprReadiedArrow.class, ItemType.class, ExpressionType.SIMPLE, "[the] (readied|selected|drawn) (:arrow|:bow)");
	}

	private boolean isArrow;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerReadyArrowEvent.class)) {
			Skript.error("Cannot use 'readied " + parseResult.tags.get(0) + "' outside of a ready arrow event", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		isArrow = parseResult.hasTag("arrow");
		return true;
	}

	@Override
	@Nullable
	protected ItemType[] get(Event event) {
		ItemStack item;
		if (isArrow) {
			item = ((PlayerReadyArrowEvent) event).getArrow();
		} else {
			item = ((PlayerReadyArrowEvent) event).getBow();
		}
		return new ItemType[]{new ItemType(item)};
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
		return "the readied " + (isArrow ? "arrow" : "bow");
	}

}
