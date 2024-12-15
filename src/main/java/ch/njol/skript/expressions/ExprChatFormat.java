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

import org.apache.commons.lang.StringUtils;
import org.bukkit.event.Event;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("Chat Format")
@Description("Can be used to get/retrieve the chat format. The sender of a message is " +
		"represented by [player] or [sender], and the message by [message] or [msg].")
@Examples({"set the chat format to \"&lt;yellow&gt;[player]&lt;light gray&gt;: &lt;green&gt;[message]\""})
@Since("2.2-dev31")
public class ExprChatFormat extends SimpleExpression<String>{
	static {
		Skript.registerExpression(ExprChatFormat.class, String.class, ExpressionType.SIMPLE, "[the] (message|chat) format[ting]");
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.RESET){
			return new Class<?>[]{String.class};
		}
		return null;
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!getParser().isCurrentEvent(AsyncPlayerChatEvent.class)){
			Skript.error("The expression 'chat format' may only be used in chat events");
			return false;
		}
		return true;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "chat format";
	}
	
	@Override
	@Nullable
	protected String[] get(Event e) {
		if (!(e instanceof AsyncPlayerChatEvent))
			return null;

		return new String[]{convertToFriendly(((AsyncPlayerChatEvent) e).getFormat())};
	}
	
	//delta[0] has to be a String unless Skript has horribly gone wrong
	@Override
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		if (delta == null || !(e instanceof AsyncPlayerChatEvent)){
			return;
		}
		String format = null;
		if (mode == Changer.ChangeMode.SET){
			String newFormat = (String) delta[0];
			if (newFormat == null){
				return;
			}
			format = convertToNormal(newFormat);
		}else if (mode == Changer.ChangeMode.RESET){
			format = "<%s> %s";
		}
		if (format == null){
			return;
		}
		((AsyncPlayerChatEvent) e).setFormat(format);
	}
	
	@SuppressWarnings({"null"}) //First parameter is marked as @NotNull and String#replaceAll won't return null
	private static String convertToNormal(String format){
		return format.replaceAll("%", "%%")
				.replaceAll("(?i)\\[(player|sender)]", "%1\\$s")
				.replaceAll("(?i)\\[(message|msg)]", "%2\\$s");
	}
	
	@SuppressWarnings({"null"}) //First parameter is marked as @NotNull and String#replaceAll won't return null
	private static String convertToFriendly(String format){
		format = format.replaceAll("%%", "%")
			.replaceAll("%1\\$s", "[player]")
			.replaceAll("%2\\$s", "[message]");
		//Format uses %s instead of %1$s and %2$s
		if (format.contains("%s")){
			if (StringUtils.countMatches(format, "%s") >= 2){
				// Format uses two %s, the order is player, message
				format = format.replaceFirst("%s", "[player]");
				format = format.replaceFirst("%s", "[message]");
			}else{
				// Format mixes %<number>$s and %s
				format = format.replaceFirst("%s", (format.contains("[player]") || format.contains("%1$s") ? "[message]" : "[player]"));
			}
		}
		return format;
	}
}
