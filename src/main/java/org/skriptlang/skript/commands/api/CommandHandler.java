package org.skriptlang.skript.commands.api;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface CommandHandler {

	boolean registerCommand(ScriptCommand scriptCommand);

	boolean unregisterCommand(ScriptCommand scriptCommand);

	@Nullable ScriptCommand getScriptCommand(String label);

	Collection<String> getScriptCommands();
	Collection<String> getServerCommands();

}
