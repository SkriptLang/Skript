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

import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Kept Items")
@Description({
	"The kept items in a 'death of player' event.",
	"Note: this expression won't remove the items from the drops when changed.",
	"Use the <a href='effects.html#EffKeepItems'>keep items</a> effect for that."
})
@Examples({
	"on death of player:",
		"\tset kept items to the drops where [input isn't victim's tool]"
})
@Events("death")
@RequiredPlugins("Paper")
@Since("INSERT VERSION")
public class ExprKeptItems extends SimpleExpression<ItemType> {

	static {
		if (Skript.methodExists(PlayerDeathEvent.class, "getItemsToKeep"))
			Skript.registerExpression(ExprKeptItems.class, ItemType.class, ExpressionType.SIMPLE, "[the] kept items");

	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerDeathEvent.class)) {
			Skript.error("'kept items' can only be used in a 'death of player' event.");
			return false;
		}
		return true;
	}

	@Override
	@Nullable 
	protected ItemType[] get(Event event) {
		if (!(event instanceof PlayerDeathEvent))
			return null;

		return ((PlayerDeathEvent) event).getItemsToKeep()
			.stream()
			.map(ItemType::new)
			.toArray(ItemType[]::new);
	}

	@Override
	@Nullable 
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case ADD:
			case REMOVE:
			case REMOVE_ALL:
			case DELETE:
			case RESET:
				return CollectionUtils.array(ItemType.class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (!(event instanceof PlayerDeathEvent))
			return;

		List<ItemStack> keptItems = ((PlayerDeathEvent) event).getItemsToKeep();
		boolean canChange = delta != null;
		// Clear items before adding the new values
		if ((mode == ChangeMode.SET && canChange) || mode == ChangeMode.DELETE || mode == ChangeMode.RESET)
			keptItems.clear();

		if (!canChange)
			return;

		for (Object o : delta) {
			ItemType item = (ItemType) o;
			switch (mode) {
				case ADD:
				case SET:
					item.addTo(keptItems);
					break;
				case REMOVE:
					item.removeFrom(keptItems);
					break;
				case REMOVE_ALL:
					item.removeAll(keptItems);
					break;
			}
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the kept items";
	}

}
