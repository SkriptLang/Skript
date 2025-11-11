package org.skriptlang.skript.common.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.RedirectingLogHandler;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Set;
import java.util.stream.Collectors;

@Name("Enable/Reload Script With Errors")
@Description("""
	Enables or reloads a script.
	Any errors occurred will be printed to console and to all players with the permission 'skript.reloadnotify'.
	
	See also: the generic 'load/enable' and 'reload' effects for loading or reloading a script without any errors being printed.
	""")
@Example("reload script \"test\"")
@Example("enable script file \"testing\"")
@Since("2.4, 2.10 (unloading)")
public class EffScriptFile extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffScriptFile.class)
				.addPatterns("(enable:(enable|load)|reload) [the] [script[s]] %scripts% with errors")
				.supplier(EffScriptFile::new)
				.build());
	}

	private Expression<Script> scriptExpression;
	private boolean isEnable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		//noinspection unchecked
		scriptExpression = (Expression<Script>) exprs[0];
		isEnable = parseResult.hasTag("enable");
		return true;
	}

	@Override
	protected void execute(Event event) {
		Set<CommandSender> recipients = Bukkit.getOnlinePlayers().stream()
			.filter(player -> player.hasPermission("skript.reloadnotify"))
			.collect(Collectors.toSet());
		RedirectingLogHandler logHandler = new RedirectingLogHandler(recipients, "").start();

		if (isEnable) {
			for (Script script : scriptExpression.getArray(event)) {
				script.load(logHandler);
			}
		} else {
			for (Script script : scriptExpression.getArray(event)) {
				script.reload(logHandler);
			}
		}
		logHandler.close();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (isEnable ? "enable " : "reload ") + scriptExpression.toString(event, debug) + " with errors";
	}

}
