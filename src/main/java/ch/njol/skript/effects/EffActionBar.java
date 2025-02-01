package ch.njol.skript.effects;

import ch.njol.skript.registrations.Classes;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.util.Kleenean;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;

@Name("Action Bar")
@Description("Sends an action bar message to the given player(s).")
@Examples("send action bar \"Hello player!\" to player")
@Since("2.3, INSERT_VERSION (object support)")
public class EffActionBar extends Effect {
	static {
		Skript.registerEffect(EffActionBar.class, "send [the] action[ ]bar [with text] %object% [to %players%]");
	}

	private Expression<?> message;

	private Expression<Player> recipients;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		message = exprs[0];
		recipients = (Expression<Player>) exprs[1];
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void execute(Event event) {
		Object msgObj = message.getSingle(event);
		if (msgObj == null)
			return;
		String msg = Classes.toString(msgObj);
		BaseComponent[] components = BungeeConverter.convert(ChatMessages.parseToArray(msg));
		for (Player player : recipients.getArray(event))
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, components);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "send action bar " + message.toString(event, debug) + " to " + recipients.toString(event, debug);
	}

}
