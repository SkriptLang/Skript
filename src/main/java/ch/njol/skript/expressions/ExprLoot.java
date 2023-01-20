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
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.inventory.ItemStack;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.world.LootGenerateEvent;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Loot")
@Description("The loot that will be generated in a 'loot generate' event.")
@Examples({
	"on loot generate:",
	"\tchance of %10",
	"\tadd 64 diamonds",
	"\tsend \"You hit the jackpot!!\""
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.16+")
public class ExprLoot extends SimpleExpression<ItemStack> {

	static {
		if (Skript.classExists("org.bukkit.event.world.LootGenerateEvent"))
			Skript.registerExpression(ExprLoot.class, ItemStack.class, ExpressionType.SIMPLE, "[the] loot");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(LootGenerateEvent.class)) {
			Skript.error("The 'loot' expression can only be used in a 'loot generate' event");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected ItemStack[] get(Event event) {
		if (!(event instanceof LootGenerateEvent))
			return new ItemStack[0];
		return ((LootGenerateEvent) event).getLoot().toArray(new ItemStack[0]);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case ADD:
			case REMOVE:
			case SET:
			case DELETE:
				return CollectionUtils.array(ItemStack[].class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (!(event instanceof LootGenerateEvent))
			return;
		LootGenerateEvent lootEvent = (LootGenerateEvent) event;

		List<ItemStack> items = null;
		if (delta != null) {
			items = new ArrayList<>(delta.length);
			for (Object item : delta)
				items.add((ItemStack) item);
		}

		switch (mode) {
			case ADD:
				lootEvent.getLoot().addAll(items);
				break;
			case REMOVE:
				lootEvent.getLoot().removeAll(items);
				break;
			case SET:
				lootEvent.setLoot(items);
				break;
			case DELETE:
				lootEvent.getLoot().clear();
				break;
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the loot";
	}

}
