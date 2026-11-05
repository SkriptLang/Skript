package org.skriptlang.skript.bukkit.command.elements.expressions;

import org.bukkit.command.CommandSender;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Command Sender")
@Description("""
	The sender of a command. \
	This differs from the command executor in that this is always the thing that originally triggered/initiated the command. \
	It cannot be changed by commands like "/execute". \
	""")
@Example("make the command sender execute \"/say hi!\"")
@Example("""
	on command:
		log "%sender% used command /%command% %arguments%" to "commands.log"
	""")
@Since("2.0")
@Events("command")
public class ExprCommandSender extends EventValueExpression<CommandSender> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprCommandSender.class, CommandSender.class, "[command['s]] sender")
				.supplier(ExprCommandSender::new)
				.build());
	}

	public ExprCommandSender() {
		super(CommandSender.class);
	}

}
