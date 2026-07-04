package org.skriptlang.skript.bukkit.command.custom;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

import java.util.Collection;

/**
 * @param script The script this command is defined in.
 * @param node The command node providing functionality for this command.
 * @param aliases Aliases used to reference the command. Can be empty.
 * @param description String describing the command.
 * @param namespace An alternative namespace the command is registered under.
 *  If null, this command is registered under the default namespace (typically {@code skript}).
 */
public record ScriptBrigadierCommand(
	Script script,
	LiteralCommandNode<CommandSourceStack> node,
	Collection<String> aliases,
	@Nullable String description,
	@Nullable String usage,
	@Nullable String namespace
) { }
