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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
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
import ch.njol.skript.util.Experience;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.IteratorIterable;

/**
 * @author Peter Güttinger
 */
@Name("Drops")
@Description("Only works in death events. Holds the drops of the dying creature. Drops can be prevented by removing them with " +
		"\"remove ... from drops\", e.g. \"remove all pickaxes from the drops\", or \"clear drops\" if you don't want any drops at all.")
@Examples({"clear drops",
		"remove 4 planks from the drops"})
@Since("1.0")
@Events("death")
public class ExprDrops extends SimpleExpression<ItemType> {

	static {
		Skript.registerExpression(ExprDrops.class, ItemType.class, ExpressionType.SIMPLE, "[the] drops");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(EntityDeathEvent.class)) {
			Skript.error("The expression 'drops' can only be used in death events", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected ItemType[] get(Event e) {
		if (!(e instanceof EntityDeathEvent))
			return null;

		return ((EntityDeathEvent) e).getDrops()
			.stream()
			.map(ItemType::new)
			.toArray(ItemType[]::new);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (getParser().getHasDelayBefore().isTrue()) {
			Skript.error("Can't change the drops anymore after the event has already passed");
			return null;
		}
		switch (mode) {
			case ADD:
			case REMOVE:
			case REMOVE_ALL:
			case SET:
				return CollectionUtils.array(ItemType[].class, Inventory[].class, Experience[].class);
			case DELETE:
			case RESET:
			default:
				return null;
		}
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (!(e instanceof EntityDeathEvent))
			return;

		List<ItemStack> drops = ((EntityDeathEvent) e).getDrops();
		assert delta != null;
		for (Object o : delta) {
			if (o instanceof Experience) {
				if (mode == ChangeMode.REMOVE_ALL || mode == ChangeMode.REMOVE && ((Experience) o).getInternalXP() == -1) {
					((EntityDeathEvent) e).setDroppedExp(0);
				} else if (mode == ChangeMode.SET) {
					((EntityDeathEvent) e).setDroppedExp(((Experience) o).getXP());
				} else {
					((EntityDeathEvent) e).setDroppedExp(Math.max(0, ((EntityDeathEvent) e).getDroppedExp() + (mode == ChangeMode.ADD ? 1 : -1) * ((Experience) o).getXP()));
				}
			} else {
				switch (mode) {
					case SET:
						drops.clear();
						//$FALL-THROUGH$
					case ADD:
						if (o instanceof Inventory) {
							for (ItemStack is : new IteratorIterable<>(((Inventory) o).iterator())) {
								if (is != null)
									drops.add(is);
							}
						} else {
							((ItemType) o).addTo(drops);
						}
						break;
					case REMOVE:
					case REMOVE_ALL:
						if (o instanceof Inventory) {
							for (ItemStack is : new IteratorIterable<>(((Inventory) o).iterator())) {
								if (is == null)
									continue;
								if (mode == ChangeMode.REMOVE)
									new ItemType(is).removeFrom(drops);
								else
									new ItemType(is).removeAll(drops);
							}
						} else {
							if (mode == ChangeMode.REMOVE)
								((ItemType) o).removeFrom(drops);
							else
								((ItemType) o).removeAll(drops);
						}
						break;
					case DELETE:
					case RESET:
						assert false;
				}
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
	public String toString(@Nullable Event e, boolean debug) {
		return "the drops";
	}

}
