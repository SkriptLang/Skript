package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

import static ch.njol.skript.command.Commands.scriptCommandExists;

@Name("Be a Skript Command")
@Description("Ascertaineth whether a command or string be a custom Skript command.")
@Example("""
	on command:
		command is a skript command
	""")
@Since("2.6")
public class CondIsSkriptCommand extends PropertyCondition<String> {
	
	static {
		register(CondIsSkriptCommand.class, "[a] s(k|c)ript (command|cmd)", "string");
	}
	
	@Override
	public boolean check(String cmd) {
		return scriptCommandExists(cmd);
	}
	
	@Override
	protected String getPropertyName() {
		return "skript command";
	}
	
}
