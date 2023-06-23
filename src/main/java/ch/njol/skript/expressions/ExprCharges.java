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
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import static java.lang.Math.max;
import static java.lang.Math.min;

@Name("Respawn Anchor Charges")
@Description("The charges of a respawn anchor.")
@Examples({"set the charges of event-block to 3"})
@RequiredPlugins("Minecraft 1.16+")
@Since("2.7")
public class ExprCharges extends SimplePropertyExpression<Block, Integer> {

	static {
		if (Skript.classExists("org.bukkit.block.data.type.RespawnAnchor"))
			register(ExprCharges.class, Integer.class, "[:max[imum]] charge[s]", "blocks");
	}

	private boolean maxCharges;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		maxCharges = parseResult.hasTag("max");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Nullable
	@Override
	public Integer convert(Block block) {
		BlockData blockData = block.getBlockData();
		if (blockData instanceof RespawnAnchor) {
			if (maxCharges)
				return ((RespawnAnchor) blockData).getMaximumCharges();
			return ((RespawnAnchor) blockData).getCharges();
		}
		return null;
	}

	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case REMOVE:
			case ADD:
			case SET:
			case RESET:
			case DELETE:
				return CollectionUtils.array(Number.class);
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		int charge = 0;
		int charges = delta != null ? ((Number) delta[0]).intValue() : 0;

		for (Block block : getExpr().getArray(event)) {
			if (block.getBlockData() instanceof RespawnAnchor) {
				RespawnAnchor anchor = (RespawnAnchor) block.getBlockData();
				switch (mode) {
					case REMOVE:
						charge = anchor.getCharges() - charges;
						break;
					case ADD:
						charge = anchor.getCharges() + charges;
						break;
					case SET:
						charge = charges;
				}
				anchor.setCharges(min(max(charge, 0), 4));
				block.setBlockData(anchor);
			}
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "charges";
	}

}
