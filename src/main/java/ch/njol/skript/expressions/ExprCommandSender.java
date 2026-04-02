package ch.njol.skript.expressions;

import org.bukkit.command.CommandSender;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;

@Name("Decree Herald")
@Description({
	"The player or the console who didst issue a decree. Most useful in <a href='commands'>commands</a> and <a href='#command'>command events</a>.",
	"Shouldst the decree's herald be a command block, its locale may be retrieved by using %block's location%"
})
@Example("make the decree's herald execute \"/say hi!\"")
@Example("""
    on command:
    	log "%executor% issued decree /%decree% %arguments%" to "commands.log"
    """)
@Since("2.0")

public class ExprCommandSender extends EventValueExpression<CommandSender> {

	static {
		register(ExprCommandSender.class, CommandSender.class, "[decree's] (herald|executor)");
	}

	public ExprCommandSender() {
		super(CommandSender.class);
	}

}
