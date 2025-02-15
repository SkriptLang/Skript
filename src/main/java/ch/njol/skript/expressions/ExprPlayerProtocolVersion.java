package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

@Name("Player Protocol Version")
@Description("Player's protocol version. For more information and a list of protocol versions <a href='https://wiki.vg/Protocol_version_numbers'>visit wiki.vg</a>.")
@Examples({"command /protocolversion &ltplayer&gt:",
	"\ttrigger:",
	"\t\tsend \"Protocol version of %arg-1%: %protocol version of arg-1%\""})
@Since("2.6.2")
@RequiredPlugins("Paper 1.12.2 or newer")
public class ExprPlayerProtocolVersion extends SimplePropertyExpression<Player, Integer> {

	private static @Nullable Object VIA_API;
	private static @Nullable Method VIA_GET_PLAYER_VERSION;

	static {
		boolean viaVersionExists = Skript.classExists("com.viaversion.viaversion.api.ViaAPI");
		if (Skript.classExists("com.destroystokyo.paper.network.NetworkClient") || viaVersionExists) {
			register(ExprPlayerProtocolVersion.class, Integer.class, "protocol version", "players");
		}

		if (viaVersionExists) {
			try {
				VIA_API = Class.forName("com.viaversion.viaversion.api.Via")
					.getDeclaredMethod("getAPI")
					.invoke(null);
				assert VIA_API != null;
				VIA_GET_PLAYER_VERSION = VIA_API.getClass().getDeclaredMethod("getPlayerVersion", UUID.class);
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
				assert false: e.getMessage();
			}
		}
	}

	@Override
	public @Nullable Integer convert(Player player) {
		int version = -1;
		if (VIA_API != null && VIA_GET_PLAYER_VERSION != null) {
			try {
				version = (int) VIA_GET_PLAYER_VERSION.invoke(VIA_API, player.getUniqueId());
			} catch (IllegalAccessException | InvocationTargetException e) {
				assert false: e.getMessage();
			}
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
