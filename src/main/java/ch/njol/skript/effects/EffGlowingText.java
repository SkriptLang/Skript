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
import org.bukkit.block.sign.Side;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Locale;

@Name("Make Sign Glow")
@Description("Makes a sign (either a block or item) have glowing text or normal text")
@Examples({
	"make target block of player have glowing text",
	"make target block of player have normal text on the front side",
})
@Since("2.8.0, INSERT VERSION (front/back)")
public class EffGlowingText extends Effect {

	private static final boolean HAS_SIDES = Skript.classExists("org.bukkit.block.sign.Side");

	static {
		String sideChoice = " [on the (:front|:back) [side]]";

		if (Skript.methodExists(Sign.class, "setGlowingText", boolean.class)) {
			Skript.registerEffect(EffGlowingText.class,
					"make %blocks/itemtypes% have glowing text" + (HAS_SIDES ? "" : sideChoice),
					"make %blocks/itemtypes% have (normal|non[-| ]glowing) text" + (HAS_SIDES ? "" : sideChoice)
			);
		}
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> objects;

	private boolean glowing;

	@Nullable
	private Side side;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		objects = exprs[0];
		glowing = matchedPattern == 0;
		side = null;
		if (parseResult.hasTag("front")) {
			side = Side.FRONT;
		} else if (parseResult.hasTag("back")) {
			side = Side.BACK;
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Object obj : objects.getArray(event)) {
			if (obj instanceof Block) {
				BlockState state = ((Block) obj).getState();
				if (state instanceof Sign) {
					setGlowingText((Sign) state);
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
				setGlowingText((Sign) state);
				state.update();
				blockMeta.setBlockState(state);
				item.setItemMeta(meta);
			}
		}
	}

	private void setGlowingText(Sign sign) {
		if (HAS_SIDES) {
			if (side == null) {
				sign.getSide(Side.FRONT).setGlowingText(glowing);
				sign.getSide(Side.BACK).setGlowingText(glowing);
			} else {
				sign.getSide(side).setGlowingText(glowing);
			}
		} else {
			sign.setGlowingText(glowing);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + objects.toString(event, debug) + " have " + (glowing ? "glowing text" : "normal text")
			+ (side == null ? "" : " on the " + side.name().toLowerCase(Locale.ENGLISH)) + " side";
	}

}
