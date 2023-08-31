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
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Apply Bone Meal")
@Description("Applies bone meal to a crop, sapling, or composter")
@Examples("apply 3 bone meal to event-block")
@RequiredPlugins("MC 1.16.2+")
@Since("INSERT VERSION")
public class EffApplyBoneMeal extends Effect {

	static {
		if (Skript.isRunningMinecraft(1, 16, 2))
			Skript.registerEffect(EffApplyBoneMeal.class, "apply [%-number%] bone[ ]meal[s] [to %blocks%]");
	}

	@Nullable
	private Expression<Number> amount;
	private Expression<Block> blocks;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		amount = (Expression<Number>) exprs[0];
		blocks = (Expression<Block>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		int times = 1;
		if (amount != null)
			times = amount.getOptionalSingle(event).orElse(0).intValue();
		for (Block block : blocks.getArray(event)) {
			for (int i = 0; i < times; i++) {
				block.applyBoneMeal(BlockFace.UP);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "apply " + amount != null ? amount.toString(event, debug) + " " : "" + "bone meal to " + blocks.toString(event, debug);
	}

}
