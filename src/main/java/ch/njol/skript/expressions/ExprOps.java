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
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("All Operators")
@Description("The list of operators on the server.")
@Examples("set {_ops::*} to all operators")
@Since("2.7")
public class ExprOps extends SimpleExpression<OfflinePlayer> {
	
	private boolean nonOps;
	
	static {
		Skript.registerExpression(ExprOps.class, OfflinePlayer.class, ExpressionType.SIMPLE, "[all [[of] the]|the] [server] [:non(-| )]op[erator]s");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		nonOps = parseResult.hasTag("non");
		return true;
	}

	@Override
	protected OfflinePlayer[] get(Event event) {
		if (nonOps) {
			List<Player> nonOpsList = new ArrayList<>();
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (!player.isOp()) 
					nonOpsList.add(player);
			}
			return nonOpsList.toArray(new Player[0]);
		}
		return Bukkit.getOperators().toArray(new OfflinePlayer[0]);
	}

	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (nonOps)
			return null;
		switch (mode) {
			case ADD:
			case SET:
			case REMOVE:
			case RESET:
			case DELETE:
				return CollectionUtils.array(OfflinePlayer[].class);
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null && mode != ChangeMode.RESET && mode != ChangeMode.DELETE)
			return;
		switch (mode) {
			case SET:
				for (OfflinePlayer player : Bukkit.getOperators())
					player.setOp(false);
			case ADD:
				for (Object player : delta)
					((OfflinePlayer) player).setOp(true);
				break;
			case REMOVE:
				for (Object player : delta)
					((OfflinePlayer) player).setOp(false);
				break;
			case DELETE:
			case RESET:
				for (OfflinePlayer player : Bukkit.getOperators())
					player.setOp(false);
				break;
			default:
				assert false;
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends OfflinePlayer> getReturnType() {
		return OfflinePlayer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (nonOps)
				return "all non-operators";
		return "all operators";
	}

}
