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
package ch.njol.skript.conditions;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Has Permission")
@Description("Test whether a player has a certain permission.")
@Examples({"player has permission \"skript.tree\"",
		"victim has the permission \"admin\":",
		"	send \"You're attacking an admin!\" to attacker"})
@Since("1.0")
public class CondPermission extends Condition {
	
	static {
		Skript.registerCondition(CondPermission.class,
				"%commandsenders% (has|have) [the] permission[s] %strings%",
				"%commandsenders% (doesn't|does not|do not|don't) have [the] permission[s] %strings%");
	}
	
	@SuppressWarnings("null")
	private Expression<String> permissions;
	@SuppressWarnings("null")
	private Expression<CommandSender> senders;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		senders = (Expression<CommandSender>) exprs[0];
		permissions = (Expression<String>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return senders.check(e,
				s -> permissions.check(e,
						perm -> {
							if (s.hasPermission(perm))
								return true;
							// player has perm skript.foo.bar if he has skript.foo.* or skript.*,
							// but not for other plugin's permissions since they can define their own *
							if (perm.startsWith("skript.")) {
								for (int i = perm.lastIndexOf('.'); i != -1; i = perm.lastIndexOf('.', i - 1)) {
									if (s.hasPermission(perm.substring(0, i + 1) + "*"))
										return true;
								}
							}
							return false;
						}), isNegated());
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return PropertyCondition.toString(this, PropertyType.HAVE, e, debug, senders,
				"the permission" + (permissions.isSingle() ? " " : "s ") + permissions.toString());
	}
	
}
