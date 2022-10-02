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

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.command.ScriptCommandEvent;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;

@Name("Cooldown Time/Remaining Time/Elapsed Time/Last Usage/Bypass Permission")
@Description({"Only usable in command events. Represents the cooldown time, the remaining time, the elapsed time,",
		"the last usage date, or the cooldown bypass permission."})
@Examples({
		"command /home:",
		"\tcooldown: 10 seconds",
		"\tcooldown message: You last teleported home %elapsed time% ago, you may teleport home again in %remaining time%.",
		"\ttrigger:",
		"\t\tteleport player to {home::%player%}"})
@Since("2.2-dev33")
public class ExprCmdCooldownInfo extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprCmdCooldownInfo.class, Object.class, ExpressionType.SIMPLE,
				"[the] remaining [time] [of [the] (cooldown|wait) [(of|for) [the] [current] command]]",
				"[the] elapsed [time] [of [the] (cooldown|wait) [(of|for) [the] [current] command]]",
				"[the] ((cooldown|wait) time|[wait] time of [the] (cooldown|wait) [(of|for) [the] [current] command])",
				"[the] last usage [date] [of [the] (cooldown|wait) [(of|for) [the] [current] command]]",
				"[the] [cooldown] bypass perm[ission] [of [the] (cooldown|wait) [(of|for) [the] [current] command]]");
	}

	private int pattern;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		pattern = matchedPattern;
		if (!getParser().isCurrentEvent(ScriptCommandEvent.class)) {
			Skript.error("The " + getExpressionName() + " expression can only be used within a command", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected Object[] get(Event e) {
		if (!(e instanceof ScriptCommandEvent))
			return null;
		ScriptCommandEvent event = ((ScriptCommandEvent) e);
		ScriptCommand scriptCommand = event.getScriptCommand();
		
		CommandSender sender = event.getSender();
		if (scriptCommand.getCooldown() == null || !(sender instanceof Player))
			return null;
		Player player = (Player) event.getSender();
		UUID uuid = player.getUniqueId();
		
		switch (pattern) {
			case 0:
			case 1:
				long ms = pattern != 1
						? scriptCommand.getRemainingMilliseconds(uuid, event)
						: scriptCommand.getElapsedMilliseconds(uuid, event);
				return new Timespan[] { new Timespan(ms) };
			case 2:
				return new Timespan[] { scriptCommand.getCooldown() };
			case 3:
				return new Date[] { scriptCommand.getLastUsage(uuid, event) };
			case 4:
				return new String[] { scriptCommand.getCooldownBypass() };
		}
		
		return null;
	}

	@Nullable
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		switch (mode) {
			case ADD:
			case REMOVE:
				if (pattern <= 1)
					// remaining or elapsed time
					return new Class<?>[] { Timespan.class };
			case RESET:
			case SET:
				if (pattern <= 1)
					// remaining or elapsed time
					return new Class<?>[] { Timespan.class };
				else if (pattern == 3)
					// last usage date
					return new Class<?>[] { Date.class };
		}
		return null;
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		if (!(e instanceof ScriptCommandEvent))
			return;
		ScriptCommandEvent commandEvent = (ScriptCommandEvent) e;
		ScriptCommand command = commandEvent.getScriptCommand();
		Timespan cooldown = command.getCooldown();
		CommandSender sender = commandEvent.getSender();
		if (cooldown == null || !(sender instanceof Player))
			return;
		long cooldownMs = cooldown.getMilliSeconds();
		UUID uuid = ((Player) sender).getUniqueId();
		
		if (pattern <= 1) {
			Timespan timespan = delta == null ? new Timespan(0) : (Timespan) delta[0];
			switch (mode) {
				case ADD:
				case REMOVE:
					long change = (mode == Changer.ChangeMode.ADD ? 1 : -1) * timespan.getMilliSeconds();
					if (pattern == 0) {
						long remaining = command.getRemainingMilliseconds(uuid, commandEvent);
						long changed = remaining + change;
						if (changed < 0)
							changed = 0;
						command.setRemainingMilliseconds(uuid, commandEvent, changed);
					} else {
						long elapsed = command.getElapsedMilliseconds(uuid, commandEvent);
						long changed = elapsed + change;
						if (changed > cooldownMs)
							changed = cooldownMs;
						command.setElapsedMilliSeconds(uuid, commandEvent, changed);
					}
					break;
				case RESET:
					if (pattern == 0)
						command.setRemainingMilliseconds(uuid, commandEvent, cooldownMs);
					else
						command.setElapsedMilliSeconds(uuid, commandEvent, 0);
					break;
				case SET:
					if (pattern == 0)
						command.setRemainingMilliseconds(uuid, commandEvent, timespan.getMilliSeconds());
					else
						command.setElapsedMilliSeconds(uuid, commandEvent, timespan.getMilliSeconds());
					break;
			}
		} else if (pattern == 3) {
			switch (mode) {
				case REMOVE_ALL:
				case RESET:
					command.setLastUsage(uuid, commandEvent, null);
					break;
				case SET:
					Date date = delta == null ? null : (Date) delta[0];
					command.setLastUsage(uuid, commandEvent, date);
					break;
			}
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		if (pattern <= 2)
			return Timespan.class;
		return pattern == 3 ? Date.class : String.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the " + getExpressionName() + " of the cooldown";
	}

	@Nullable
	private String getExpressionName() {
		switch (pattern) {
			case 0:
				return "remaining time";
			case 1:
				return "elapsed time";
			case 2:
				return "cooldown time";
			case 3:
				return "last usage date";
			case 4:
				return "bypass permission";
		}
		return null;
	}

}
