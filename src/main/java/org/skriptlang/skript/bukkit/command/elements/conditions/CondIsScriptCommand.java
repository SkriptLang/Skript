package org.skriptlang.skript.bukkit.command.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandRegistrar;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is a Script Command")
@Description("Checks whether a command (string label) is a script command (one registered in a script).")
@Example("""
	on command:
		command is not a script command
		# do something only for non-custom commands
	""")
@Since("2.6")
public class CondIsScriptCommand extends PropertyCondition<String> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.CONDITION,
			infoBuilder(CondIsScriptCommand.class, PropertyType.BE, "[a] s(c|k)ript (command|cmd)", "string")
				.supplier(CondIsScriptCommand::new)
				.build());
	}

	@Override
	public boolean check(String command) {
		return ScriptCommandRegistrar.getCommand(command) != null;
	}

	@Override
	protected String getPropertyName() {
		return "script command";
	}

}
