package ch.njol.skript.expressions;

import org.bukkit.command.CommandSender;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;

@Name("Command Sender")
@Description({
	"The player or the console who sent a command. Mostly useful in <a href='commands'>commands</a> and <a href='#command'>command events</a>.",
	"If the command sender is a command block, its location can be retrieved by using %block's location%"
})
@Example("make the command sender execute \"/say hi!\"")
@Example("""
	on command:
		log "%executor% used command /%command% %arguments%" to "commands.log"
	""")
@Since("2.0")
@Events("command")
public class ExprCommandSender extends EventValueExpression<CommandSender> {

	static {
		register(ExprCommandSender.class, CommandSender.class, "[command['s]] (sender|executor)");
	}

	public ExprCommandSender() {
		super(CommandSender.class);
	}

}
