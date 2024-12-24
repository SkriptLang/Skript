package ch.njol.skript.conditions;

import static ch.njol.skript.command.Commands.scriptCommandExists;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is a Skript command")
@Description("Checks whether a command/string is a custom Skript command.")
@Examples(
	{"# Example 1",
	"on command:", 
	"\tcommand is a skript command",
	"",
	"# Example 2",
	"\"sometext\" is a skript command"})
@Since("2.6")
public class CondIsSkriptCommand extends PropertyCondition<String> {
	
	static {
		register(CondIsSkriptCommand.class, PropertyType.BE, "[a] s(k|c)ript (command|cmd)", "string");
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
