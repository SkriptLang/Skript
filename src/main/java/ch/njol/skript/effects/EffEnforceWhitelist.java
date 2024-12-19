/**
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.effects;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Since;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;

@Name("Enforce Whitelist")
@Description({
	"Enforces or un-enforce a server's whitelist.",
	"All non-whitelisted players will be kicked upon enforcing the whitelist."
})
@Examples({
	"enforce the whitelist",
	"unenforce the whitelist"
})
@Since("2.9.0")
@RequiredPlugins("MC 1.17+")
public class EffEnforceWhitelist extends Effect {

	private static String NOT_WHITELISTED_MESSAGE = "You are not whitelisted on this server!";

	static {
		if (Skript.methodExists(Bukkit.class, "setWhitelistEnforced", boolean.class)) {
			try {
				YamlConfiguration spigotYml = YamlConfiguration.loadConfiguration(new File("spigot.yml"));
				NOT_WHITELISTED_MESSAGE = spigotYml.getString("messages.whitelist", NOT_WHITELISTED_MESSAGE);
			} catch (Exception ignored) {}
			Skript.registerEffect(EffEnforceWhitelist.class, "[:un]enforce [the] [server] white[ ]list");
		}
	}

	private boolean enforce;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		enforce = !parseResult.hasTag("un");
		return true;
	}

	@Override
	protected void execute(Event event) {
		Bukkit.setWhitelistEnforced(enforce);
		reloadWhitelist();
	}

	// A workaround for Bukkit's not kicking non-whitelisted players upon enforcement
	public static void reloadWhitelist() {
		Bukkit.reloadWhitelist();
		if (!Bukkit.hasWhitelist() || !Bukkit.isWhitelistEnforced())
			return;
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!player.isWhitelisted() && !player.isOp())
				player.kickPlayer(Utils.replaceChatStyles(NOT_WHITELISTED_MESSAGE));
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return (!enforce ? "un" : "") + "enforce the whitelist";
	}

}
