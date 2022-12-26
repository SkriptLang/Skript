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

import java.util.ArrayList;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Toggle")
@Description("Toggle a boolean or the state of a block.")
@Examples({"# use arrows to toggle switches, doors, etc.",
		"on projectile hit:",
		"\tprojectile is arrow",
		"\ttoggle the block at the arrow",
		"",
		"# With booleans",
		"toggle gravity of player"
})
@Since("1.4, INSERT VERSION (booleans)")
public class EffToggle extends Effect {
	
	static {
		Skript.registerEffect(EffToggle.class, 
				"(close|turn off|de[-]activate) %blocks%",
		 		"(toggle|switch) [[the] state of] %booleans/blocks%",
		  		"(open|turn on|activate) %blocks%");
	}

	@SuppressWarnings("null")
	private Expression<?> toggledExpr;
	private int toggle;
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		toggledExpr = (Expression<?>) vars[0];
		toggle = matchedPattern - 1;
		if (toggledExpr.getReturnType() == Boolean.class && !ChangerUtils.acceptsChange(toggledExpr, ChangeMode.SET, Boolean.class)) {
			Skript.error(toggledExpr.toString(null, false) + " cannot be toggled");
			return false;
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		ArrayList<Object> toggledValues = new ArrayList<>();
		for (Object obj : toggledExpr.getArray(event)) {
			if (obj instanceof Block) {
				Block block = (Block) obj;
				BlockData data = block.getBlockData();
				if (toggle == 0) {
					if (data instanceof Openable) // open = NOT was open
						((Openable) data).setOpen(!((Openable) data).isOpen());
					else if (data instanceof Powerable) // power = NOT power
						((Powerable) data).setPowered(!((Powerable) data).isPowered());
				} else {
					if (data instanceof Openable)
						((Openable) data).setOpen(toggle == 1);
					else if (data instanceof Powerable)
						((Powerable) data).setPowered(toggle == 1);
				}

				block.setBlockData(data);
				toggledValues.add(block);

			} else if (obj instanceof Boolean) {
				toggledValues.add(!(Boolean) obj);
			}
		}

		toggledExpr.change(event, toggledValues.toArray(), ChangeMode.SET);
		
	}
	@Override
	public String toString(@Nullable Event event, final boolean debug) {
		return "toggle " + toggledExpr.toString(event, debug);
	}
	
}
