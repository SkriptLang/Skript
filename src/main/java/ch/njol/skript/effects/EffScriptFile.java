package ch.njol.skript.effects;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptCommand;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.PluralizingArgsMessage;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.LogHandler;
import ch.njol.skript.log.RedirectingLogHandler;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.TimingLogHandler;
import ch.njol.skript.registrations.Feature;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.script.Script;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.FileUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.OpenCloseable;
import org.bukkit.event.Event;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Name("Enable/Disable/Unload/Reload Script")
@Description("""
	Enables, disables, unloads, or reloads a script.
	
	Disabling a script unloads it and prepends - to its name so it will not be loaded the next time the server restarts.
	If the script reflection experiment is enabled: unloading a script terminates it and removes it from memory, but does not alter the file.""")
@Example("reload script \"test\"")
@Example("enable script file \"testing\"")
@Example("unload script file \"script.sk\"")
@Example("""
	set {_script} to the script "MyScript.sk"
	reload {_script}
	""")
@Since("2.4, 2.10 (unloading)")
public class EffScriptFile extends Effect {

	static {
		Skript.registerEffect(EffScriptFile.class,
			"(1:(enable|load)|2:reload|3:disable|4:unload) script [file|named] %string% [print:with errors]",
			"(1:(enable|load)|2:reload|3:disable|4:unload) skript file %string% [print:with errors]",
			"(1:(enable|load)|2:reload|3:disable|4:unload) %scripts% [print:with errors]"
		);
		/*
			The string-pattern must come first (since otherwise `script X` would match the expression)
			and we cannot get a script object for a non-loaded script.
		 */
	}

	private static final int ENABLE = 1, RELOAD = 2, DISABLE = 3, UNLOAD = 4;

	private int mark;

	private @UnknownNullability Expression<String> scriptNameExpression;
	private @UnknownNullability Expression<Script> scriptExpression;
	private boolean scripts, hasReflection, printErrors;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		this.mark = parseResult.mark;
		printErrors = parseResult.hasTag("print");
		switch (matchedPattern) {
			case 0, 1:
				this.scriptNameExpression = (Expression<String>) exprs[0];
				break;
			case 2:
				this.scriptExpression = (Expression<Script>) exprs[0];
				this.scripts = true;
		}
		this.hasReflection = this.getParser().hasExperiment(Feature.SCRIPT_REFLECTION);
		return true;
	}

	@Override
	protected void execute(Event event) {
		Set<CommandSender> recipients = new HashSet<>();
		if (printErrors) {
			recipients.addAll(Bukkit.getOnlinePlayers().stream()
				.filter(player -> player.hasPermission("skript.reloadnotify"))
				.collect(Collectors.toSet()));
		}
		RedirectingLogHandler logHandler = new RedirectingLogHandler(recipients, "").start();

		if (scripts) {
			for (Script script : scriptExpression.getArray(event)) {
				@Nullable File file = script.getConfig().getFile();
				this.handle(file, script.getConfig().getFileName(), logHandler);
			}
		} else {
			String name = scriptNameExpression.getSingle(event);
			if (name != null)
				this.handle(ScriptLoader.getScriptFromName(name), name, logHandler);
		}
		logHandler.close();
	}

	private void handle(@Nullable File scriptFile, @Nullable String name, OpenCloseable openCloseable) {
		if (scriptFile == null || !scriptFile.exists())
			return;
		if (name == null)
			name = scriptFile.getName();
		FileFilter filter = ScriptLoader.getDisabledScriptsFilter();
		switch (mark) {
			case ENABLE:
				if (ScriptLoader.getLoadedScripts().contains(ScriptLoader.getScript(scriptFile)))
					return;
				if (filter.accept(scriptFile)) {
					try {
						// TODO Central methods to be used between here and SkriptCommand should be created for
						//  enabling/disabling (renaming) files
						scriptFile = FileUtils.move(
							scriptFile,
							new File(scriptFile.getParentFile(), scriptFile.getName()
								.substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH)),
							false
						);
					} catch (IOException ex) {
						//noinspection ThrowableNotThrown
						Skript.exception(ex, "Error while enabling script file: " + name);
						return;
					}
				}

				ScriptLoader.loadScripts(scriptFile, openCloseable);
				break;
			case RELOAD:
				if (filter.accept(scriptFile))
					return;

				this.unloadScripts(scriptFile);

				ScriptLoader.loadScripts(scriptFile, openCloseable);
				break;
			case UNLOAD:
				if (hasReflection) { // if we don't use the new features this falls through into DISABLE
					if (!ScriptLoader.getLoadedScriptsFilter().accept(scriptFile))
						return;

					this.unloadScripts(scriptFile);
					break;
				}
			case DISABLE:
				if (filter.accept(scriptFile))
					return;

				this.unloadScripts(scriptFile);

				try {
					FileUtils.move(
						scriptFile,
						new File(scriptFile.getParentFile(), ScriptLoader.DISABLED_SCRIPT_PREFIX + scriptFile.getName()),
						false
					);
				} catch (IOException ex) {
					//noinspection ThrowableNotThrown
					Skript.exception(ex, "Error while disabling script file: " + name);
					return;
				}
				break;
			default:
				assert false;
		}
	}

	private void unloadScripts(File file) {
		Set<Script> loaded = ScriptLoader.getLoadedScripts();
		if (file.isDirectory()) {
			Set<Script> scripts = ScriptLoader.getScripts(file);
			if (scripts.isEmpty())
				return;
			scripts.retainAll(loaded); // skip any that are not loaded (avoid throwing error)
			ScriptLoader.unloadScripts(scripts);
		} else {
			Script script = ScriptLoader.getScript(file);
			if (!loaded.contains(script))
				return; // don't need to unload if not loaded (avoid throwing error)
			if (script != null)
				ScriptLoader.unloadScript(script);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String start = switch (mark) {
			case ENABLE -> "enable";
			case DISABLE -> "disable";
			case RELOAD -> "reload";
			default -> "unload";
		} + " ";
		if (scripts)
			return start + scriptExpression.toString(event, debug);
		return start + "script file " + scriptNameExpression.toString(event, debug);
	}

}
