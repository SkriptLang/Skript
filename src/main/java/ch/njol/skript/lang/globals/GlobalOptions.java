package ch.njol.skript.lang.globals;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.lang.OptionRegistry;
import org.skriptlang.skript.addon.SkriptAddon;

import java.io.IOException;
import java.util.*;

/**
 * Represents the 'globals/options.sk' file
 */
public class GlobalOptions extends GlobalFile {

	public GlobalOptions(SkriptAddon addon) {
		super(addon, "options");
	}

	/**
	 * Loads the 'globals/options.sk' file
	 */
	@Override
	public void load() {
		try {
			Config config = new Config(file, true, false, ":");
			config.getMainNode().convertToEntries(-1);
			OptionRegistry optionRegistry = getAddon().registry(OptionRegistry.class);
			optionRegistry.loadGlobalOptions(config.getMainNode());
		} catch (IOException e) {
			Skript.exception(e, "Error while loading global options");
		}
	}

}
