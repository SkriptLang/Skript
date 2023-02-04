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

import ch.njol.skript.effects.EffEnforceWhitelist;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

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

@Name("Whitelist")
@Description({
	"A server's whitelist.",
	"This expression can be used to add/remove players to/from the whitelist.",
	"To enable and disable it (set whitelist to true/false), and to empty it (reset whitelist)"
})
@Examples({
	"set whitelist to false",
	"add all players to whitelist",
	"reset the whitelist"
})
@Since("2.5.2")
public class ExprWhitelist extends SimpleExpression<OfflinePlayer> {

	private EffEnforceWhitelist effEnforceWhitelist = new EffEnforceWhitelist();

	static {
		Skript.registerExpression(ExprWhitelist.class, OfflinePlayer.class, ExpressionType.SIMPLE, "[the] white[ ]list");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected OfflinePlayer[] get(Event event) {
		return Bukkit.getServer().getWhitelistedPlayers().toArray(new OfflinePlayer[0]);
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE) {
			return CollectionUtils.array(OfflinePlayer[].class);
		} else if (mode == ChangeMode.SET || mode == ChangeMode.RESET) {
			return CollectionUtils.array(Boolean.class);
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		switch (mode) {
			case SET:
				Bukkit.setWhitelist((Boolean) delta[0]);
				effEnforceWhitelist.reloadWhitelist();
				break;
			case ADD:
				for (Object player : delta)
					((OfflinePlayer) player).setWhitelisted(true);
				break;
			case REMOVE:
				for (Object player : delta)
					((OfflinePlayer) player).setWhitelisted(false);
				break;
			case RESET:
				for (OfflinePlayer player : Bukkit.getWhitelistedPlayers()) {
					player.setWhitelisted(false);
				}
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
		return "whitelist";
	}

}
