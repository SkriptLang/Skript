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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

@Name("Player Protocol Version")
@Description("Player's protocol version. For more information and list of protocol versions <a href='https://wiki.vg/Protocol_version_numbers'>visit wiki.vg</a>.")
@Examples({"command /protocolversion &ltplayer&gt:",
	"\ttrigger:",
	"\t\tsend \"Protocol version of %arg-1%: %protocol version of arg-1%\""})
@Since("2.6.2, INSERT VERSION ViaVersion support")
@RequiredPlugins("Paper 1.12.2 or newer")
public class ExprPlayerProtocolVersion extends SimplePropertyExpression<Player, Integer> {

	private static final boolean VIAVERSION_EXISTS = Skript.classExists("com.viaversion.viaversion.api.ViaAPI");
	@Nullable
	private static Object VIA_API;

	static {
		if (Skript.classExists("com.destroystokyo.paper.network.NetworkClient") || VIAVERSION_EXISTS) {
			register(ExprPlayerProtocolVersion.class, Integer.class, "protocol version", "players");
		}

		if (VIAVERSION_EXISTS) {
            try {
                VIA_API = Class.forName("com.viaversion.viaversion.api.Via")
					.getDeclaredMethod("getAPI")
					.invoke(null);
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {}
        }
	}

	@Override
	@Nullable
	public Integer convert(Player player) {
		int version = -1;
		if (VIAVERSION_EXISTS && VIA_API != null) {
			try {
				Method getPlayerVersion = VIA_API.getClass().getDeclaredMethod("getPlayerVersion", UUID.class);
				version = (int) getPlayerVersion.invoke(VIA_API, player.getUniqueId());
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {}
        } else {
			version = player.getProtocolVersion();
		}
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
