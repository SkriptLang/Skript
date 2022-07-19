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
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

@Name("Player Protocol Version")
@Description("Player's protocol version. For more information and list of protocol versions <a href='https://wiki.vg/Protocol_version_numbers'>visit wiki.vg</a>.")
@Examples({"command /protocolversion &ltplayer&gt:",
	"\ttrigger:",
	"\t\tsend \"Protocol version of %arg-1%: %protocol version of arg-1%\""})
@Since("2.6.2")
@RequiredPlugins("Paper 1.12.2 or newer")
public class ExprPlayerProtocolVersion extends SimplePropertyExpression<Player, Integer> {

	static {
		if (Skript.classExists("com.destroystokyo.paper.network.NetworkClient")) {
			register(ExprPlayerProtocolVersion.class, Integer.class, "protocol version", "players");
		}
	}

	@Override
	@Nullable
	public Integer convert(Player player) {
		int version = player.getProtocolVersion();
		return version == -1 ? null : version;
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "protocol version";
	}

}
