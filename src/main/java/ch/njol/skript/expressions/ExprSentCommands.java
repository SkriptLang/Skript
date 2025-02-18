package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.EvtPlayerCommandSend;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.common.collect.Lists;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Name("Sent Command List")
@Description({
	"The commands that will be sent to the player in a <a href='events.html#send_command_list'>send commands to player event</a>.",
	"Modifications will affect what commands show up for the player to tab complete. They will not affect what commands the player can actually run.",
	"Adding new commands to the list is illegal behavior and will be ignored."
})
@Examples({
	"on send command list:",
		"\tset command list to command list where [input does not contain \":\"]",
		"\tremove \"help\" from command list"
})
@Since("2.8.0")
@Events("send command list")
public class ExprSentCommands extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprSentCommands.class, String.class, ExpressionType.SIMPLE, "[the] [sent] [server] command[s] list");
	}

	private EvtPlayerCommandSend parent;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		Structure structure = getParser().getCurrentStructure();
		if (!(structure instanceof EvtPlayerCommandSend)) {
			Skript.error("The 'command list' expression can only be used in a 'send command list' event");
			return false;
		}

		if (!isDelayed.isFalse()) {
			Skript.error("Can't change the command list after the event has already passed");
			return false;
		}
		parent = (EvtPlayerCommandSend) structure;
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		if (!(event instanceof PlayerCommandSendEvent))
			return null;
		return ((PlayerCommandSendEvent) event).getCommands().toArray(new String[0]);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case REMOVE:
			case DELETE:
			case SET:
			case RESET:
				return new Class[]{String[].class};
			// note that ADD is not supported, as adding new commands to the commands collection is illegal behaviour
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (!(event instanceof PlayerCommandSendEvent))
			return;

		Collection<String> commands = ((PlayerCommandSendEvent) event).getCommands();

		// short circuit if we're just clearing the list
		if (mode == ChangeMode.DELETE) {
			commands.clear();
			return;
		}

		List<String> deltaCommands = (delta != null && delta.length > 0) ? Lists.newArrayList((String[]) delta) : new ArrayList<>();
		switch (mode) {
			case REMOVE:
				commands.removeAll(deltaCommands);
				break;
			case SET:
				// remove all completely new commands, as adding new commands to the commands collection is illegal behaviour
				List<String> newCommands = new ArrayList<>(deltaCommands);
				newCommands.removeAll(parent.getOriginalCommands());
				deltaCommands.removeAll(newCommands);
				commands.clear();
				commands.addAll(deltaCommands);
				break;
			case RESET:
				commands.clear();
				commands.addAll(parent.getOriginalCommands());
				break;
			default:
				assert false;
				break;
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the sent server command list";
	}

}
