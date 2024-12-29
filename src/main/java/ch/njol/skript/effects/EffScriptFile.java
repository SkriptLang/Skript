package ch.njol.skript.effects;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptCommand;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import org.skriptlang.skript.lang.script.Script;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.FileUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.OpenCloseable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Set;

@Name("Enable/Disable/Reload Script File")
@Description("Enables, disables, or reloads a script file.")
@Examples({
	"reload script \"test\"",
	"enable script file \"testing\"",
	"unload script file \"script.sk\""
})
@Since("2.4")
public class EffScriptFile extends Effect {

	static {
		Skript.registerEffect(EffScriptFile.class,
			"(1:(enable|load)|2:reload|3:(disable|unload)) s(c|k)ript [file] %string%"
		);
	}
	
	private static final int ENABLE = 1, RELOAD = 2, DISABLE = 3;
	
	private int mark;

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<String> fileName;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		mark = parseResult.mark;
		fileName = (Expression<String>) exprs[0];
		return true;
	}
	
	@Override
	protected void execute(Event e) {
		String name = fileName.getSingle(e);
		if (name == null)
			return;
		File scriptFile = SkriptCommand.getScriptFromName(name);
		if (scriptFile == null)
			return;

		switch (mark) {
			case ENABLE: {
				if (ScriptLoader.getLoadedScriptsFilter().accept(scriptFile))
					return;

				try {
					// TODO Central methods to be used between here and SkriptCommand should be created for enabling/disabling (renaming) files
					scriptFile = FileUtils.move(
						scriptFile,
						new File(scriptFile.getParentFile(), scriptFile.getName().substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH)),
						false
					);
				} catch (IOException ex) {
					//noinspection ThrowableNotThrown
					Skript.exception(ex, "Error while enabling script file: " + name);
					return;
				}

				ScriptLoader.loadScripts(scriptFile, OpenCloseable.EMPTY);
				break;
			}
			case RELOAD: {
				if (ScriptLoader.getDisabledScriptsFilter().accept(scriptFile))
					return;
				
				this.unloadScripts(scriptFile);
				
				ScriptLoader.loadScripts(scriptFile, OpenCloseable.EMPTY);
				break;
			}
			case DISABLE: {
				if (ScriptLoader.getDisabledScriptsFilter().accept(scriptFile))
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
			}
			default:
				assert false;
		}
	}
	
	private void unloadScripts(File file) {
		if (file.isDirectory()) {
			Set<Script> scripts = ScriptLoader.getScripts(file);
			if (scripts.isEmpty())
				return;
			ScriptLoader.unloadScripts(scripts);
		} else {
			Script script = ScriptLoader.getScript(file);
			if (script != null)
				ScriptLoader.unloadScript(script);
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return (mark == ENABLE ? "enable" : mark == RELOAD ? "disable" : mark == DISABLE ? "unload" : "")
			+ " script file " + fileName.toString(e, debug);
	}

}
