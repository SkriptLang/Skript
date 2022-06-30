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

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Age of Block/Entity")
@Description({
	"Returns the age or max age of blocks and age for entities (there in no max age for entities).",
	"For blocks, 'Age' represents the different growth stages that a crop-like block can go through. " +
	"A value of 0 indicates that the crop was freshly planted, whilst a value equal to 'maximum age' indicates that the crop is ripe and ready to be harvested.",
	"For entities, 'Age' represents the time left for them to become adults and it is minus until it reached 0 which means they're adults, " +
	"e.g. A baby cow needs 20 minutes to become an adult which equals to 24,000 ticks so their age will be -23999 once spawned."
})
@Examples({
	"# Set targeted crop to fully grown crop",
	"set age of targeted block to max age of targeted block",
	" ",
	"# Spawn a baby cow that will only need 1 minute to become an adult",
	"spawn a baby cow at player",
	"set age of last spawned entity to -1200 # in ticks = 60 seconds"
})
@RequiredPlugins("Minecraft 1.13+")
@Since("INSERT VERSION")
public class ExprAge extends SimplePropertyExpression<Object, Integer> {
	
	static {
		register(ExprAge.class, Integer.class, "[:max[imum]] age", "blocks/entities");
	}

	private boolean isMax = false;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isMax = parseResult.hasTag("max");
		setExpr(exprs[0]);
		return true;
	}

	@Override
	@Nullable
	public Integer convert(Object obj) {
		if (obj instanceof Block) {
			Ageable ageable = (Ageable) ((Block) obj).getBlockData();
			return isMax ? ageable.getMaximumAge() : ageable.getAge();
		} else if (obj instanceof org.bukkit.entity.Ageable) {
			return  ((org.bukkit.entity.Ageable) obj).getAge();
		}

		return null;
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return !isMax && (mode == ChangeMode.SET || mode == ChangeMode.RESET) ? CollectionUtils.array(Number.class) : null;
	}
	
	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (mode == ChangeMode.SET && (delta == null || delta[0] == null))
			return;

		int value = mode == ChangeMode.RESET ? 0 : ((Number) delta[0]).intValue();
		for (Object obj : getExpr().getArray(event)) {
			if (obj instanceof Block) {
				Block block = (Block) obj;
				BlockData bd = block.getBlockData();
				if (bd instanceof Ageable) {
					((Ageable) bd).setAge(Math.max(Math.min(value, ((Ageable) bd).getMaximumAge()), 0));
					block.setBlockData(bd);
				}
			} else if (obj instanceof org.bukkit.entity.Ageable) {
				// Bukkit accepts higher values that 0, they will keep going down to 0 though (some Animal type might be using that - not sure)
				((org.bukkit.entity.Ageable) obj).setAge(value);
			}
		}
	}
	
	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}
	
	@Override
	protected String getPropertyName() {
		return (isMax ? "max " : "") + "age";
	}

}
