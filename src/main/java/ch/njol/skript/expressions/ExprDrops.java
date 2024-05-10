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
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

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
			Skript.error("Can't change the drops after the event has already passed");
			return null;
		}
		switch (mode) {
			case ADD:
			case REMOVE:
			case REMOVE_ALL:
			case SET:
				return CollectionUtils.array(ItemType[].class, Inventory[].class, Experience[].class);
			case DELETE: // handled by EffClearDrops
			case RESET:
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (!(event instanceof EntityDeathEvent))
			return;

		List<ItemStack> drops = ((EntityDeathEvent) event).getDrops();
		int originalExperience = ((EntityDeathEvent) event).getDroppedExp();
		assert delta != null;

		// separate the delta into experience and drops to make it easier to handle
		int deltaExperience = -1; // Skript does not support negative experience, so -1 is a safe "unset" value
		boolean removeAllExperience = false;
		List<ItemType> deltaDrops = new ArrayList<>();
		for (Object o : delta) {
			if (o instanceof Experience) {
				// Special case for `remove xp from the drops`
				if ((((Experience) o).getInternalXP() == -1 && mode == ChangeMode.REMOVE) || mode == ChangeMode.REMOVE_ALL) {
					removeAllExperience = true;
				}
				// add the value even if we're removing all experience, just so we know that experience was changed
				if (deltaExperience == -1) {
					deltaExperience = ((Experience) o).getXP();
				} else {
					deltaExperience += ((Experience) o).getXP();
				}
			} else if (o instanceof Inventory) {
				// inventories are unrolled into their contents
				for (ItemStack item : ((Inventory) o).getContents()) {
					if (item != null)
						deltaDrops.add(new ItemType(item));
				}
			} else if (o instanceof ItemType) {
				deltaDrops.add((ItemType) o);
			} else {
				assert false;
			}
		}

		// handle items and experience separately to maintain current behavior
		// `set drops to iron sword` should not affect experience
		// and `set drops to 1 xp` should not affect items
		// todo: All the experience stuff should be removed from this class for 2.8 and given to ExprExperience

		// handle experience
		if (deltaExperience > -1) {
			switch (mode) {
				case SET:
					((EntityDeathEvent) event).setDroppedExp(deltaExperience);
					break;
				case ADD:
					((EntityDeathEvent) event).setDroppedExp(originalExperience + deltaExperience);
					break;
				case REMOVE:
					((EntityDeathEvent) event).setDroppedExp(originalExperience - deltaExperience);
					// fallthrough to check for removeAllExperience
				case REMOVE_ALL:
					if (removeAllExperience)
						((EntityDeathEvent) event).setDroppedExp(0);
					break;
				case DELETE:
				case RESET:
					assert false;
			}
		}

		// handle items
		if (!deltaDrops.isEmpty()) {
			switch (mode) {
				case SET:
					// clear drops and fallthrough to add
					drops.clear();
				case ADD:
					for (ItemType item : deltaDrops) {
						item.addTo(drops);
					}
					break;
				case REMOVE:
					for (ItemType item : deltaDrops) {
						item.removeFrom(false, drops);
					}
					break;
				case REMOVE_ALL:
					for (ItemType item : deltaDrops) {
						item.removeAll(false, drops);
					}
					break;
				case DELETE:
				case RESET:
					assert false;
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
		return "the drops";
	}

}
