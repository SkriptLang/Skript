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
package ch.njol.skript.expressions;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Whitelist")
@Description({
	"A server's whitelist or whitelist enforcement.",
	"This expression can be used to add/remove players to/from the whitelist.",
	"To enable and disable it (set whitelist to true / set whitelist to false), and to empty it (reset whitelist)",
	"To enable and disable enforcement (set whitelist enforcement to true / set whitelist enforcement to false),",
	"which kicks all non-whitelisted players once set to true."
})
@Examples({"set whitelist to false",
	"add all players to whitelist",
	"reset the whitelist"})
@Since("2.5.2, INSERT VERSION (enforce)")
@RequiredPlugins("Minecraft 1.17+")
public class ExprWhitelist extends SimpleExpression<OfflinePlayer> {

	private static final boolean ENFORCE_SUPPORT;

	static {
		ENFORCE_SUPPORT = Skript.methodExists(Bukkit.class, "setWhitelistEnforced", boolean.class);
		Skript.registerExpression(ExprWhitelist.class, OfflinePlayer.class, ExpressionType.SIMPLE, "[the] white[ ]list"
			+ (ENFORCE_SUPPORT ? " [:enforcement]" : ""));
	}

	private boolean isEnforce;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isEnforce = parseResult.hasTag("enforcement");
		return true;
	}

	@Override
	protected OfflinePlayer[] get(Event event) {
		return (!isEnforce ? Bukkit.getServer().getWhitelistedPlayers().toArray(new OfflinePlayer[0]) : new OfflinePlayer[0]);
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE) {
			if (isEnforce) {
				if (ENFORCE_SUPPORT)
					Skript.error("\"Whitelist enforcement\" can't have anything " + (mode == ChangeMode.ADD ? "added" : "removed") + "." +
						" Use 'whitelist' instead.");
				return null;
			}
			return CollectionUtils.array(OfflinePlayer[].class);
		} else if (mode == ChangeMode.SET || mode == ChangeMode.RESET) {
			if (mode == ChangeMode.RESET && isEnforce)
				return null;
			return CollectionUtils.array(Boolean.class);
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		switch (mode) {
			case SET:
				boolean value = (boolean) delta[0];
				if (isEnforce)
					Bukkit.setWhitelistEnforced(value);
				else
					Bukkit.setWhitelist(value);
				if (ENFORCE_SUPPORT)
					reloadWhitelist();
				break;
			case ADD:
				for (Object p : delta)
					((OfflinePlayer) p).setWhitelisted(true);
				break;
			case REMOVE:
				for (Object p : delta)
					((OfflinePlayer) p).setWhitelisted(false);
				if (ENFORCE_SUPPORT)
					reloadWhitelist();
				break;
			case RESET:
				for (OfflinePlayer p : Bukkit.getWhitelistedPlayers()) {
					p.setWhitelisted(false);
				}
				break;
			default:
				assert false;
		}
	}

	private void reloadWhitelist() {
		Bukkit.reloadWhitelist();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!player.isWhitelisted() && !player.isOp())
				player.kickPlayer("You are not whitelisted on this server!");
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
		return "whitelist" + (isEnforce ? " enforcement" : "");
	}

}
