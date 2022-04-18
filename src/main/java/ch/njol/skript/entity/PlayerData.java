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
package ch.njol.skript.entity;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Consumer;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 */
public class PlayerData extends EntityData<Player> {
	static {
		EntityData.register(PlayerData.class, "player", Player.class, 1, "non-op", "player", "op");
	}
	
	// used by EntityData.getAll to efficiently get all players
	int op = 0;
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		op = matchedPattern - 1;
		return true;
	}
	
	@Override
	protected boolean init(final @Nullable Class<? extends Player> c, final @Nullable Player e) {
		op = e == null ? 0 : e.isOp() ? 1 : -1;
		return true;
	}
	
	@Override
	public void set(final Player p) {
		if (op != 0)
			p.setOp(op == 1);
	}
	
	@Override
	protected boolean match(final Player p) {
		return op == 0 || p.isOp() == (op == 1);
	}
	
	@Override
	public Class<? extends Player> getType() {
		return Player.class;
	}

	@Override
	@Nullable
	public Player spawn(Location loc, @Nullable Consumer<Player> consumer) {
		return null;
	}

	@Override
	protected int hashCode_i() {
		return op;
	}
	
	@Override
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof PlayerData))
			return false;
		final PlayerData other = (PlayerData) obj;
		return op == other.op;
	}
	
//		return "" + op;
	@Override
	protected boolean deserialize(final String s) {
		try {
			op = Integer.parseInt(s);
			return true;
		} catch (final NumberFormatException e) {
			return false;
		}
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (e instanceof PlayerData)
			return op == 0 || ((PlayerData) e).op == op;
		return false;
	}
	
	@Override
	public EntityData getSuperType() {
		return new PlayerData();
	}
	
}
