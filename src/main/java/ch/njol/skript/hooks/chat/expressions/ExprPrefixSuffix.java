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
package ch.njol.skript.hooks.chat.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.hooks.VaultHook;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Prefix/Suffix")
@Description("The prefix or suffix as defined in the server's chat plugin.")
@Examples({
	"on chat:",
	"\tcancel event",
	"\tbroadcast \"%player's prefix%%player's display name%%player's suffix%: %message%\" to the player's world",
	"",
	"set the player's prefix to \"[&lt;red&gt;Admin<reset>] \""
})
@Since("2.0")
@RequiredPlugins({"Vault", "a chat plugin that supports Vault"})
public class ExprPrefixSuffix extends SimplePropertyExpression<Player, String> {
	static {
		register(ExprPrefixSuffix.class, String.class, "[chat] (1¦prefix|2¦suffix)", "players");
	}
	
	private boolean prefix;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		prefix = parseResult.mark == 1;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}
	
	@Override
	public String convert(final Player p) {
		return Utils.replaceChatStyles(prefix ? "" + VaultHook.chat.getPlayerPrefix(p) : "" + VaultHook.chat.getPlayerSuffix(p));
	}
	
	@Override
	protected String getPropertyName() {
		return prefix ? "prefix" : "suffix";
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return new Class[] {String.class};
		return null;
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		assert mode == ChangeMode.SET;
		assert delta != null;
		for (final Player p : getExpr().getArray(e)) {
			if (prefix)
				VaultHook.chat.setPlayerPrefix(p, (String) delta[0]);
			else
				VaultHook.chat.setPlayerSuffix(p, (String) delta[0]);
		}
	}
	
}
