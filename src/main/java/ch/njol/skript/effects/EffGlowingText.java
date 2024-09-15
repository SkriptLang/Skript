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
package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

@Name("Make Sign Glow")
@Description("Makes a sign (either a block or item) have glowing text or normal text")
@Examples("make target block of player have glowing text")
@Since("2.8.0")
public class EffGlowingText extends Effect {

	static {
		if (Skript.methodExists(Sign.class, "setGlowingText", boolean.class)) {
			Skript.registerEffect(EffGlowingText.class,
					"make %blocks/itemtypes% have glowing text",
					"make %blocks/itemtypes% have (normal|non[-| ]glowing) text"
			);
		}
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> objects;

	private boolean glowing;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		objects = exprs[0];
		glowing = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Object obj : objects.getArray(event)) {
			if (obj instanceof Block) {
				BlockState state = ((Block) obj).getState();
				if (state instanceof Sign) {
					((Sign) state).setGlowingText(glowing);
					state.update();
				}
			} else if (obj instanceof ItemType) {
				ItemType item = (ItemType) obj;
				ItemMeta meta = item.getItemMeta();
				if (!(meta instanceof BlockStateMeta))
					return;
				BlockStateMeta blockMeta = (BlockStateMeta) meta;
				BlockState state = blockMeta.getBlockState();
				if (!(state instanceof Sign))
					return;
				((Sign) state).setGlowingText(glowing);
				state.update();
				blockMeta.setBlockState(state);
				item.setItemMeta(meta);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + objects.toString(event, debug) + " have " + (glowing ? "glowing text" : "normal text");
	}

}
