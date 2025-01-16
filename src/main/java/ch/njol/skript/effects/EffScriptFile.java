package ch.njol.skript.effects;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptCommand;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.SharedConfig;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.registrations.Feature;
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
import java.util.Set;

@Name("Enable/Disable/Unload/Reload Script")
@Description("""
	Enables, disables, unloads, or reloads a script.
	
	Disabling a script unloads it and prepends - to its name so it will not be loaded the next time the server restarts.
	If the script reflection experiment is enabled: unloading a script terminates it and removes it from memory, but does not alter the file.""")
@Examples({
	"reload script \"test\"",
	"enable script file \"testing\"",
	"unload script file \"script.sk\"",
	"set {_script} to the script \"MyScript.sk\"",
	"reload {_script}"
})
@Since("2.4, 2.10 (unloading)")
public class EffScriptFile extends Effect {

	static {
		Skript.registerEffect(EffScriptFile.class,
			"(1:(enable|load)|2:reload|3:disable|4:unload) script [file|named] %string%",
			"(1:(enable|load)|2:reload|3:disable|4:unload) skript file %string%",
			"(1:(enable|load)|2:reload|3:disable|4:unload) %scripts%",
			"(1:load|2:reload|4:unload|5:save) %configs%"
		);
		/*
			The string-pattern must come first (since otherwise `script X` would match the expression)
			and we cannot get a script object for a non-loaded script.
		 */
	}

	private static final int ENABLE = 1, RELOAD = 2, DISABLE = 3, UNLOAD = 4, SAVE = 5;

	private int mark;

	private @UnknownNullability Expression<String> scriptNameExpression;
	private @UnknownNullability Expression<Script> scriptExpression;
	private @UnknownNullability Expression<Config> configExpression;
	private boolean scripts, configs, hasReflection;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		this.mark = parseResult.mark;
		switch (matchedPattern) {
			case 0, 1:
				this.scriptNameExpression = (Expression<String>) exprs[0];
				break;
			case 2:
				this.scriptExpression = (Expression<Script>) exprs[0];
				this.scripts = true;
				break;
			case 3:
				this.configExpression = (Expression<Config>) exprs[0];
				this.configs = true;
		}
		this.hasReflection = this.getParser().hasExperiment(Feature.SCRIPT_REFLECTION);
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (configs) {
			Config[] array = configExpression.getArray(event);
			for (Config config : array)
				this.handle(config);
		} else if (scripts) {
			Script[] array = scriptExpression.getArray(event);
			for (Script script : array) {
				@Nullable File file = script.getConfig().getFile();
				this.handle(file, script.getConfig().getFileName());
			}
		} else {
			String name = scriptNameExpression.getSingle(event);
			if (name == null)
				return;
			this.handle(ScriptLoader.getScriptFromName(name), name);
		}
	}

	private void handle(Config config) {
		if (config instanceof SharedConfig) {
			SharedConfig shared = (SharedConfig) config;
			switch (mark) {
				case SAVE:
					shared.save();
					break;
				case ENABLE:
					shared.load();
					break;
				case RELOAD:
					shared.reload();
					break;
				case UNLOAD:
					shared.unload();
					break;
			}
		} else {
			switch (mark) {
				case SAVE: {
					try {
						@Nullable File file = config.getFile();
						if (file != null)
							config.save(file);
					} catch (IOException ignored) {
					}
					break;
				}
				case ENABLE:
				case RELOAD:
					if (config == SkriptConfig.getConfig()) {
						SkriptConfig.load();
						break;
					}
				default:
					this.handle(config.getFile(), config.getFileName());
			}
		}
	}

	private void handle(@Nullable File scriptFile, @Nullable String name) {
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

				ScriptLoader.loadScripts(scriptFile, OpenCloseable.EMPTY);
				break;
			case RELOAD:
				if (filter.accept(scriptFile))
					return;

				this.unloadScripts(scriptFile);

				ScriptLoader.loadScripts(scriptFile, OpenCloseable.EMPTY);
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
