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
package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Locale;

@Name("Has Glowing Text")
@Description("Checks whether a sign (either a block or an item) has glowing text.")
@Examples({
	"if target block has glowing text:",
	"if target block doesn't have glowing text on the back side:"
})
@Since("2.8.0, INSERT VERSION (front/back)")
public class CondGlowingText extends PropertyCondition<Object> {

	// 1.19 has Side, but doesn't have Side.BACK, so ensure we're at least 1.20
	private static final boolean HAS_SIDES = Skript.classExists("org.bukkit.block.sign.Side") && Skript.isRunningMinecraft(1,20);

	static {
		String sideChoice = " [on the (:front|:back) [side]]";

		if (Skript.methodExists(Sign.class, "isGlowingText")) {
			register(CondGlowingText.class, PropertyType.HAVE, "glowing text" + (HAS_SIDES ? "" : sideChoice), "blocks/itemtypes");
		}
	}

	@Nullable
	private Side side;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		side = null;
		if (parseResult.hasTag("front")) {
			side = Side.FRONT;
		} else if (parseResult.hasTag("back")) {
			side = Side.BACK;
		}
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(Object obj) {
		if (obj instanceof Block) {
			BlockState state = ((Block) obj).getState();
			return state instanceof Sign && isGlowing((Sign) state);
		} else if (obj instanceof ItemType) {
			ItemMeta meta = ((ItemType) obj).getItemMeta();
			if (meta instanceof BlockStateMeta) {
				BlockState state = ((BlockStateMeta) meta).getBlockState();
				return state instanceof Sign && isGlowing((Sign) state);
			}
		}
		return false;
	}

	private boolean isGlowing(Sign sign) {
		if (HAS_SIDES) {
			if (side == null)
				return sign.getSide(Side.FRONT).isGlowingText() || sign.getSide(Side.BACK).isGlowingText();
			return sign.getSide(side).isGlowingText();
		}
		return sign.isGlowingText();
	}

	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.HAVE;
	}

	@Override
	protected String getPropertyName() {
		return "glowing text"
			+ (side == null ? "" : " on the " + side.name().toLowerCase(Locale.ENGLISH)) + " side";
	}

}
